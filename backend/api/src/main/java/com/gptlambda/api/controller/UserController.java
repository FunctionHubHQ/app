package com.gptlambda.api.controller;

import com.gptlambda.api.service.user.UserService;
import com.gptlambda.api.UserApi;
import com.gptlambda.api.UserProfileResponse;
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
  public ResponseEntity<UserProfileResponse> getUserprofile() {
    return ResponseEntity.ok(userService.getOrCreateUserprofileAsync());
  }
}
