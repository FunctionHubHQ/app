package net.functionhub.api.service.user;

import java.time.ZoneOffset;
import java.util.List;
import net.functionhub.api.ApiKey;
import net.functionhub.api.ApiKeyRequest;
import net.functionhub.api.ApiKeyResponse;
import net.functionhub.api.data.postgres.entity.ApiKeyEntity;
import net.functionhub.api.data.postgres.entity.EntitlementEntity;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.repo.ApiKeyRepo;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.props.EntitlementProps;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.security.firebase.SecurityFilter;
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
  private final SecurityFilter securityFilter;
  private final UserRepo userRepo;
  private final ApiKeyRepo apiKeyRepo;
  private final EntitlementRepo entitlementRepo;
  private final EntitlementProps entitlementProps;


  @Override
  public UserProfileResponse getOrCreateUserprofile() {
    UserProfile userProfile = securityFilter.getUser();
    Thread.startVirtualThread(() -> createDbUser(userProfile));
    return new UserProfileResponse().profile(userProfile);
  }

  @Override
  public void createDbUser(UserProfile userProfile) {
    if (userProfile != null && !ObjectUtils.isEmpty(userProfile.getUid())) {
      UserEntity entity = userRepo.findByUid(userProfile.getUid());
      if (entity == null) {
        UserEntity newUser = new UserEntity();
        newUser.setEmail(userProfile.getEmail());
        newUser.setUid(userProfile.getUid());
        newUser.setFullName(userProfile.getName());
        log.info("Creating new user with  uid = {}", newUser.getUid());
        userRepo.save(newUser);

        ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
        apiKeyEntity.setApiKey(generateApiKey());
        apiKeyEntity.setUserId(newUser.getUid());
        apiKeyRepo.save(apiKeyEntity);

        EntitlementEntity entitlements = new EntitlementEntity();
        entitlements.setUid(UUID.randomUUID());
        entitlements.setUserId(userProfile.getUid());
        entitlements.setTimeout(entitlementProps.getWallTime());
        entitlements.setFunctions(entitlementProps.getFunctions());
        entitlements.setTokens(entitlementProps.getTokens());
        entitlements.setHttpEgress(entitlementProps.getHttpEgress());
        entitlements.setDailyInvocations(entitlementProps.getDailyInvocations());
        entitlementRepo.save(entitlements);
      }
    }
  }

  @Override
  public ApiKeyResponse getApiKeys() {
    List<ApiKeyEntity> apiKeyEntities = apiKeyRepo.findByUserIdOrderByCreatedAtDesc(FHUtils.getSessionUser().getUid());
    if (ObjectUtils.isEmpty(apiKeyEntities)) {
      // A user needs at least 1 api key to access FunctionHub
      // CAUTION: This call may enter a recursive loop if upsert fails for some reason
      return upsertApiKey(new ApiKeyRequest());
    } else {
      return new ApiKeyResponse().keys(apiKeyEntities
              .stream()
              .map(it -> {
                ApiKey apiKey = new ApiKey();
                apiKey.setKey(it.getIsVendorKey() ? redactString(it.getApiKey()) : it.getApiKey());
                apiKey.setIsVendorKey(it.getIsVendorKey());
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
  public ApiKeyResponse upsertApiKey(ApiKeyRequest apiKeyRequest) {
    ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
    if (ObjectUtils.isEmpty(apiKeyRequest.getKey())) {
      apiKeyEntity.setIsVendorKey(false);
      apiKeyEntity.setApiKey(generateApiKey());
    } else {
      apiKeyEntity.setIsVendorKey(true);
      apiKeyEntity.setApiKey(apiKeyRequest.getKey());
    }
    apiKeyEntity.setUserId(FHUtils.getSessionUser().getUid());
    apiKeyRepo.save(apiKeyEntity);
    return getApiKeys();
  }

  @Override
  public ApiKeyResponse deleteKey(ApiKeyRequest apiKeyRequest) {
    if (apiKeyRequest.getIsVendorKey() != null && apiKeyRequest.getIsVendorKey()) {
      apiKeyRepo.deleteAll(
          apiKeyRepo.findByIsVendorKeyAndUserId(true, FHUtils.getSessionUser().getUid()));
    } else if (!ObjectUtils.isEmpty(apiKeyRequest.getKey())) {
        ApiKeyEntity entity = apiKeyRepo.findByApiKey(apiKeyRequest.getKey());
        if (entity != null) {
          apiKeyRepo.delete(entity);
          List<ApiKeyEntity> apiKeyEntities = apiKeyRepo.findByUserIdOrderByCreatedAtDesc(
              FHUtils.getSessionUser().getUid());
          if (apiKeyEntities.stream().filter(it -> !it.getIsVendorKey()).toList().size() == 0) {
            // A user must always have at least one FunctionHub key
            return upsertApiKey(new ApiKeyRequest());
          }
        }
    }
    return getApiKeys();
  }

  private String generateApiKey() {
    return apiKeyPrefix + FHUtils.generateUid(FHUtils.API_KEY_LENGTH);
  }
}
