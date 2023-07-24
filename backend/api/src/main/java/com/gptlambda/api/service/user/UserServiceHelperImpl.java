package com.gptlambda.api.service.user;

import com.gptlambda.api.data.postgres.repo.UserRepo;
import com.gptlambda.api.data.postgres.entity.UserEntity;
import com.gptlambda.api.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 12/6/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceHelperImpl implements UserServiceHelper {
  private final UserRepo userRepo;

  @Async
  @Override
  public void createDbUserAsync(UserProfile userProfile) {
    createDbUserHelper(userProfile);
  }

  @Override
  public void createDbUser(UserProfile userProfile) {
    createDbUserHelper(userProfile);
  }

  private void createDbUserHelper(UserProfile userProfile) {
    if (userProfile != null && !ObjectUtils.isEmpty(userProfile.getUid())) {
      UserEntity entity = userRepo.findByUid(userProfile.getUid());
      if (entity == null) {
        UserEntity newUser = new UserEntity();
        newUser.setEmail(userProfile.getEmail());
        newUser.setUid(userProfile.getUid());
        log.info("Creating new user with firebase ID = {}", newUser.getUid());
        userRepo.save(newUser);

      }
    } else {
      throw new RuntimeException("Please logout and login again to update your account");
    }
  }
}
