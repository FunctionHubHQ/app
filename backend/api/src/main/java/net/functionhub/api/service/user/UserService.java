package net.functionhub.api.service.user;

import net.functionhub.api.ApiKeyRequest;
import net.functionhub.api.ApiKeyResponse;
import net.functionhub.api.UserProfile;
import net.functionhub.api.UserProfileResponse;
import net.functionhub.api.UsernameRequest;
import net.functionhub.api.UsernameResponse;

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

  ApiKeyResponse getApiKeys();

  ApiKeyResponse createNewApiKey(ApiKeyRequest apiKeyRequest);

  ApiKeyResponse deleteKey(ApiKeyRequest apiKeyRequest);

  UsernameResponse usernameExists(UsernameRequest usernameRequest);

  UserProfileResponse updateUsername(UsernameRequest usernameRequest);

  /**
   * Create an anonymous user with limited privileges. This user
   * is for making requests to endpoints that would have been unsecure.
   * We are creating an anonymous user instead because of how deeply
   * integrated the session user object is into the services.
   */
  void createAnonymousUser();
}
