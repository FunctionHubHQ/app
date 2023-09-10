package net.functionhub.api.controller;

import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.ExecResultAsync;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.RuntimeApi;
import net.functionhub.api.SpecApi;
import net.functionhub.api.SpecResult;
import net.functionhub.api.StatusRequest;
import net.functionhub.api.StatusResponse;
import net.functionhub.api.service.runtime.RuntimeService;
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
public class RuntimeController implements RuntimeApi, SpecApi {
  private final RuntimeService runtimeService;

  @Override
  public ResponseEntity<ExecResultAsync> exec(ExecRequest execRequest) {
//    return ResponseEntity.ok(runtimeService.exec(execRequest, true));
    throw new RuntimeException("Deprecated");
  }

  @Override
  public ResponseEntity<Code> getCodeDetail(String uid, Boolean bySlug) {
    return ResponseEntity.ok(runtimeService.getCodeDetail(uid, bySlug));
  }

  @Override
  public ResponseEntity<ExecResultAsync> getExecResult(String execId) {
    return ResponseEntity.ok(runtimeService.getExecutionResult(execId));
  }

  @Override
  public ResponseEntity<GenericResponse> deploy(ExecRequest execRequest) {
    return ResponseEntity.ok(runtimeService.deploy(execRequest));
  }

  @Override
  public ResponseEntity<String> getUserCode(String hash) {
    return ResponseEntity.ok(runtimeService.getUserCode(hash));
  }

  @Override
  public ResponseEntity<StatusResponse> getSpecStatus(StatusRequest statusRequest) {
    return ResponseEntity.ok(runtimeService.getSpecStatus(statusRequest));
  }

  @Override
  public ResponseEntity<GenericResponse> handleExecResult(ExecResultAsync execResult) {
    return ResponseEntity.ok(runtimeService.handleExecResult(execResult));
  }

  @Override
  public ResponseEntity<GenericResponse> handleSpecResult(SpecResult specResult) {
    return ResponseEntity.ok(runtimeService.handleSpecResult(specResult));
  }

  @Override
  public ResponseEntity<String> runDevFunction(String functionSlug, String body) {
    return ResponseEntity.ok(runtimeService.runDevFunction(functionSlug, body));
  }

  @Override
  public ResponseEntity<String> runProdFunction(String functionSlug, String body) {
    return ResponseEntity.ok(runtimeService.runProdFunction(functionSlug, body));
  }

  @Override
  public ResponseEntity<CodeUpdateResult> updateCode(Code code) {
    return ResponseEntity.ok(runtimeService.updateCode(code, false, false));
  }

  @Override
  public ResponseEntity<String> getUserSpec(String functionId, String version, String authToken, String env) {
    return ResponseEntity.ok(runtimeService.getUserSpec(functionId, version, env));
  }
}