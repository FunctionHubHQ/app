package net.functionhub.api.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.InternalApi;
import net.functionhub.api.service.internal.InternalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse created on 9/5/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalController implements InternalApi {
  private final InternalService internalService;

  @Override
  public ResponseEntity<GenericResponse> logHttpRequests(Map<String, Object> requestBody) {
    return ResponseEntity.ok(internalService.logHttpRequests(requestBody));
  }
}
