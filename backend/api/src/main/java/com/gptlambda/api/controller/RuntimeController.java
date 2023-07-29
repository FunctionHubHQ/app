package com.gptlambda.api.controller;

import com.gptlambda.api.Code;
import com.gptlambda.api.CodeUpdateResponse;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResult;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.RuntimeApi;
import com.gptlambda.api.SpecResult;
import com.gptlambda.api.service.runtime.RuntimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse
 * created on 7/24/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RuntimeController implements RuntimeApi {
  private final RuntimeService runtimeService;

  @Override
  public ResponseEntity<GenericResponse> exec(ExecRequest execRequest) {
    return ResponseEntity.ok(runtimeService.exec(execRequest));
  }

  @Override
  public ResponseEntity<Code> getCodeDetail(String uid) {
    return ResponseEntity.ok(runtimeService.getCodeDetail(uid));
  }

  @Override
  public ResponseEntity<String> deleteMe(String uid) {
    return ResponseEntity.ok(runtimeService.deleteMe(uid));
  }

  @Override
  public ResponseEntity<String> getUserCode(String hash) {
    return ResponseEntity.ok(runtimeService.getUserCode(hash));
  }

  @Override
  public ResponseEntity<GenericResponse> handleExecResult(ExecResult execResult) {
    return ResponseEntity.ok(runtimeService.handleExecResult(execResult));
  }

  @Override
  public ResponseEntity<GenericResponse> handleSpecResult(SpecResult specResult) {
    return ResponseEntity.ok(runtimeService.handleSpecResult(specResult));
  }

  @Override
  public ResponseEntity<CodeUpdateResponse> updateCode(Code code) {
    return ResponseEntity.ok(runtimeService.updateCode(code));
  }
}