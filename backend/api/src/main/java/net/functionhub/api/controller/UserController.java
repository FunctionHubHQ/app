package net.functionhub.api.controller;

import net.functionhub.api.ApiKeyRequest;
import net.functionhub.api.ApiKeyResponse;
import net.functionhub.api.UsernameRequest;
import net.functionhub.api.UsernameResponse;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.UserApi;
import net.functionhub.api.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse
 * created on 10/17/22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
  private final UserService userService;

  @Override
  public ResponseEntity<ApiKeyResponse> deleteKey(ApiKeyRequest apiKeyRequest) {
    return ResponseEntity.ok(userService.deleteKey(apiKeyRequest));
  }

  @Override
  public ResponseEntity<ApiKeyResponse> getApiKeys() {
    return ResponseEntity.ok(userService.getApiKeys());
  }

  @Override
  public ResponseEntity<String> getEnvVariables() {
    return ResponseEntity.ok(userService.getEnvVariables());
  }

  @Override
  public ResponseEntity<UserProfileResponse> getUserprofile() {
    return ResponseEntity.ok(userService.getOrCreateUserprofile());
  }

  @Override
  public ResponseEntity<UserProfileResponse> updateUsername(UsernameRequest usernameRequest) {
    return ResponseEntity.ok(userService.updateUsername(usernameRequest));
  }

  @Override
  public ResponseEntity<ApiKeyResponse> upsertApiKey(ApiKeyRequest apiKeyRequest) {
    return ResponseEntity.ok(userService.createNewApiKey(apiKeyRequest));
  }

  @Override
  public ResponseEntity<String> upsertEnvVariables(String requestBody) {
    return ResponseEntity.ok(userService.upsertEnvVariables(requestBody));
  }

  @Override
  public ResponseEntity<UsernameResponse> usernameExists(UsernameRequest usernameRequest) {
    return ResponseEntity.ok(userService.usernameExists(usernameRequest));
  }
}
