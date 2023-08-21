package net.functionhub.api.service.user;

import com.beust.ah.A;
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
        apiKeyEntity.setApiKey(apiKeyPrefix + FHUtils.generateUid(46));
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
}
