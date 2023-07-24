package com.gptlambda.api.controller;

import com.gptlambda.api.ExtensionApi;
import com.gptlambda.api.ExtensionRequest;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.service.chromeExtension.ExtensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse
 * created on 5/25/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ExtensionController implements ExtensionApi {

  private final ExtensionService extensionService;

  @Override
  public ResponseEntity<GenericResponse> handleMessage(ExtensionRequest extensionRequest) {
    Thread.startVirtualThread(() -> extensionService.handleMessages(extensionRequest));
    return ResponseEntity.ok(new GenericResponse().status("OK"));
  }
}