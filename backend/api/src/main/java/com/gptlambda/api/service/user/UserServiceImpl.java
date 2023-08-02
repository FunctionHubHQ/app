package com.gptlambda.api.service.user;

import com.gptlambda.api.data.postgres.entity.EntitlementEntity;
import com.gptlambda.api.data.postgres.entity.UserEntity;
import com.gptlambda.api.data.postgres.repo.EntitlementRepo;
import com.gptlambda.api.data.postgres.repo.UserRepo;
import com.gptlambda.api.props.EntitlementProps;
import com.gptlambda.api.service.utils.GPTLambdaUtils;
import com.gptlambda.api.utils.security.firebase.SecurityFilter;
import com.gptlambda.api.UserProfile;
import com.gptlambda.api.UserProfileResponse;
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
        newUser.setApiKey(apiKeyPrefix + GPTLambdaUtils.generateUid(46));
        log.info("Creating new user with  uid = {}", newUser.getUid());
        userRepo.save(newUser);
        EntitlementEntity entitlements = new EntitlementEntity();
        entitlements.setUid(UUID.randomUUID());
        entitlements.setUserId(userProfile.getUid());
        entitlements.setTimeout(entitlementProps.getWallTime());
        entitlements.setFunctions(entitlements.getFunctions());
        entitlements.setTokens(entitlements.getTokens());
        entitlements.setHttpEgress(entitlementProps.getHttpEgress());
        entitlements.setDailyInvocations(entitlements.getDailyInvocations());
        entitlementRepo.save(entitlements);
      }
    }
  }
}
