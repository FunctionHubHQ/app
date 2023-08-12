package net.functionhub.api.service.user;

import net.functionhub.api.UserProfile;
import net.functionhub.api.UserProfileResponse;

/**
 * @author Biz Melesse
 * created on 10/17/22
 */
public interface UserService {

  String apiKeyPrefix = "fh-";

  enum AuthMode {
    JWT, // FunctionHub-generated JWT
    AK, // FunctionHub-generated API ky
    FB // Firebase
  }

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
