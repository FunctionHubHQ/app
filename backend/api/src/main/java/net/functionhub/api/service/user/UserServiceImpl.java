package net.functionhub.api.service.user;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import net.functionhub.api.ApiKey;
import net.functionhub.api.ApiKeyProvider;
import net.functionhub.api.ApiKeyRequest;
import net.functionhub.api.ApiKeyResponse;
import net.functionhub.api.UsernameRequest;
import net.functionhub.api.UsernameResponse;
import net.functionhub.api.data.postgres.entity.ApiKeyEntity;
import net.functionhub.api.data.postgres.entity.EntitlementEntity;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.repo.ApiKeyRepo;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.data.postgres.repo.ProjectRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.dto.SessionUser;
import net.functionhub.api.props.DefaultConfigsProps;
import net.functionhub.api.props.EntitlementProps;
import net.functionhub.api.service.runtime.Slugify;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.UserProfile;
import net.functionhub.api.UserProfileResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse
 * created on 10/17/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final ProjectRepo projectRepo;
  private final UserRepo userRepo;
  private final ApiKeyRepo apiKeyRepo;
  private final EntitlementRepo entitlementRepo;
  private final EntitlementProps entitlementProps;
  private final DefaultConfigsProps defaultConfigsProps;
  private final Slugify slugify;

  @Override
  public UserProfileResponse getOrCreateUserprofile() {
    SessionUser sessionUser = FHUtils.getSessionUser();
    UserProfile userProfile = FHUtils.getUserProfile(
        projectRepo.findAllUserProjects(sessionUser.getUserId())
    );
    createDbUser(userProfile);
    return new UserProfileResponse().profile(userProfile);
  }

  @Override
  public void createDbUser(UserProfile userProfile) {
    if (userProfile != null && !ObjectUtils.isEmpty(userProfile.getUserId())) {
      Optional<UserEntity> entity = userRepo.findById(userProfile.getUserId());
      if (entity.isEmpty()) {
        UserEntity newUser = new UserEntity();
        newUser.setEmail(userProfile.getEmail());
        newUser.setId(userProfile.getUserId());
        newUser.setFullName(userProfile.getName());
        newUser.setAvatarUrl(userProfile.getPicture());
        userRepo.save(newUser);
        log.info("Created new user with  ID = {}", newUser.getId());

        ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
        apiKeyEntity.setId(FHUtils.generateEntityId("api"));
        apiKeyEntity.setApiKey(generateApiKey());
        apiKeyEntity.setUserId(newUser.getId());
        apiKeyEntity.setProvider(ApiKeyProvider.FUNCTION_HUB.getValue());
        apiKeyRepo.save(apiKeyEntity);

        EntitlementEntity entitlements = new EntitlementEntity();
        entitlements.setId(FHUtils.generateEntityId("et"));
        entitlements.setUserId(userProfile.getUserId());
        entitlements.setMaxExecutionTime(entitlementProps.getFree().getMaxExecutionTime());
        entitlements.setMaxCpuTime(entitlementProps.getFree().getMaxCpuTime());
        entitlements.setMaxMemoryUsage(entitlementProps.getFree().getMaxMemoryUsage());
        entitlements.setMaxDataTransfer(entitlementProps.getFree().getMaxBandwidth());
        entitlements.setMaxHttpCalls(entitlementProps.getFree().getNumHttpCalls());
        entitlements.setMaxInvocations(entitlementProps.getFree().getNumInvocations());
        entitlements.setMaxFunctions(entitlementProps.getFree().getNumFunctions());
        entitlements.setMaxProjects(entitlementProps.getFree().getNumProjects());
        entitlementRepo.save(entitlements);
      }
    }
  }

  @Override
  public ApiKeyResponse getApiKeys() {
    List<ApiKeyEntity> apiKeyEntities = apiKeyRepo.findAllByProviders(
        List.of(
            ApiKeyProvider.FUNCTION_HUB.getValue(),
            ApiKeyProvider.OPEN_AI.getValue()),
        FHUtils.getSessionUser().getUserId());
    if (ObjectUtils.isEmpty(apiKeyEntities)) {
      // A user needs at least 1 api key to access FunctionHub
      // CAUTION: This call may enter a recursive loop if upsert fails for some reason
      return createNewApiKey(new ApiKeyRequest().provider(ApiKeyProvider.FUNCTION_HUB));
    } else {
      return new ApiKeyResponse().keys(apiKeyEntities
              .stream()
              .map(it -> {
                ApiKey apiKey = new ApiKey();
                if (it.getProvider().equals(ApiKeyProvider.FUNCTION_HUB.getValue())) {
                  apiKey.setKey(it.getApiKey());
                } else {
                  apiKey.setKey(redactString(it.getApiKey()));
                }
                apiKey.setProvider(ApiKeyProvider.fromValue(it.getProvider()));
                apiKey.setCreatedAt(it.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
                return apiKey;
      }).toList());
    }
  }

  private String redactString(String value) {
    if (value != null && value.length() < FHUtils.API_KEY_LENGTH) {
      return  "*".repeat(FHUtils.LONG_UID_LENGTH);
    }
    return  "*".repeat(value.length());
  }

  @Override
  public ApiKeyResponse createNewApiKey(ApiKeyRequest apiKeyRequest) {
    ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
    apiKeyEntity.setId(FHUtils.generateEntityId("api"));
    if (ObjectUtils.isEmpty(apiKeyRequest.getKey())) {
      apiKeyEntity.setProvider(ApiKeyProvider.FUNCTION_HUB.getValue());
      apiKeyEntity.setApiKey(generateApiKey());
    } else {
      apiKeyEntity.setProvider(apiKeyRequest.getProvider().getValue());
      apiKeyEntity.setApiKey(apiKeyRequest.getKey());
    }
    apiKeyEntity.setUserId(FHUtils.getSessionUser().getUserId());
    apiKeyRepo.save(apiKeyEntity);
    return getApiKeys();
  }

  @Override
  public ApiKeyResponse deleteKey(ApiKeyRequest apiKeyRequest) {
    if (ObjectUtils.isEmpty(apiKeyRequest.getKey())) {
      apiKeyRepo.deleteAll(
          apiKeyRepo.findAllByProvider(apiKeyRequest.getProvider().getValue(),
              FHUtils.getSessionUser().getUserId()));
    } else if (!ObjectUtils.isEmpty(apiKeyRequest.getKey())) {
        ApiKeyEntity entity = apiKeyRepo.findByApiKey(apiKeyRequest.getKey());
        if (entity != null) {
          apiKeyRepo.delete(entity);
          List<ApiKeyEntity> apiKeyEntities = apiKeyRepo.findAllByProvider(
              ApiKeyProvider.FUNCTION_HUB.getValue(),
              FHUtils.getSessionUser().getUserId());
          if (apiKeyEntities.size() == 0) {
            // A user must always have at least one FunctionHub key
            return createNewApiKey(new ApiKeyRequest());
          }
        }
    }
    return getApiKeys();
  }

  @Override
  public UsernameResponse usernameExists(UsernameRequest usernameRequest) {
    if (!ObjectUtils.isEmpty(usernameRequest)) {
      String username = cleanUsername(usernameRequest.getUsername());
      int minUserNameLength = 4;
      int maxUserNameLength = 255;
      if (username.length() >= minUserNameLength && username.length() < maxUserNameLength) {
        int count = userRepo.findUsernameCount(username);
        return new UsernameResponse()
            .isAvailable(count == 0);
      }
    }
    return new UsernameResponse()
        .isAvailable(false);
  }

  @Override
  public UserProfileResponse updateUsername(UsernameRequest usernameRequest) {
    SessionUser sessionUser = FHUtils.getSessionUser();
    UserProfile profile = new UserProfile();
    if (!ObjectUtils.isEmpty(usernameRequest) &&
        !ObjectUtils.isEmpty(usernameRequest.getUsername())) {
      String username = cleanUsername(usernameRequest.getUsername());
      Optional<UserEntity> entity = userRepo.findById(sessionUser.getUserId());
      if (entity.isPresent()) {
        entity.get().setUsername(username);
        userRepo.save(entity.get());
        profile.setUsername(username);
      }
    }
    return new UserProfileResponse().profile(profile);
  }

  @Override
  public void createAnonymousUser() {
    UserEntity userEntity = userRepo.findByEmail(defaultConfigsProps.getAnonEmail());
    if (userEntity == null) {
      userEntity = new UserEntity();
      userEntity.setAnonymous(true);
      userEntity.setUsername("anon-" + UUID.randomUUID());
      userEntity.setEmail(defaultConfigsProps.getAnonEmail());
      userEntity.setFullName("Anon");
      userEntity.setId(FHUtils.generateEntityId("u"));
      userEntity.setIsPremiumUser(false);
      userRepo.save(userEntity);

      ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
      apiKeyEntity.setId(FHUtils.generateEntityId("api"));
      apiKeyEntity.setApiKey(defaultConfigsProps.getAnonApiKey());
      apiKeyEntity.setUserId(userEntity.getId());
      apiKeyEntity.setProvider(ApiKeyProvider.FUNCTION_HUB.getValue());
      apiKeyRepo.save(apiKeyEntity);

      EntitlementEntity entitlements = new EntitlementEntity();
      entitlements.setId(FHUtils.generateEntityId(EntitlementEntity.class.getName()));
      entitlements.setUserId(userEntity.getId());
      entitlementRepo.save(entitlements);
    }
  }

  private String cleanUsername(String username) {
    return slugify.toSlug(username)
        .strip()
        .replace("-", "_");
  }

  private String generateApiKey() {
    return apiKeyPrefix + FHUtils.generateUid(FHUtils.API_KEY_LENGTH);
  }
}
