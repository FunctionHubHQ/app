package com.gptlambda.api.service.user;

import com.gptlambda.api.UserProfile;
import com.gptlambda.api.UserProfileResponse;

/**
 * @author Biz Melesse
 * created on 10/17/22
 */
public interface UserService {

  String apiKeyPrefix = "gl-";

  /**
   * Get session user profile. If the user does not exist, create a new record.
   * @return
   */
  UserProfileResponse getOrCreateUserprofile();

  /**
   * Create a db user with default entitlements
   */
  void createDbUser(UserProfile userProfile);
}
