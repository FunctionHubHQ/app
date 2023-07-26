package com.gptlambda.api.controller;

import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.RuntimeApi;
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
  public ResponseEntity<String> getUserCode(String hash) {
    return ResponseEntity.ok(runtimeService.getUserCode(hash));
//    return ResponseEntity.ok("import moment from \"npm:moment\";\n"
//        + "\n"
//        + "interface RequestPayload {\n"
//        + "  greeting?: String,\n"
//        + "  day?: String\n"
//        + "}\n"
//        + "\n"
//        + "export default async function(payload: RequestPayload) {\n"
//        + "  console.log(\"Default function reached in user code\", xyz)\n"
//        + "  return `${payload.day}: ${payload.greeting}, time: ${moment().format('MMMM Do YYYY, h:mm:ss a')}`\n"
//        + "}");
  }
}