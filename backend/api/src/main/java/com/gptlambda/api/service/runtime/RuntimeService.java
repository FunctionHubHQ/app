package com.gptlambda.api.service.runtime;

import com.gptlambda.api.Code;
import com.gptlambda.api.CodeUpdateResponse;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResultAsync;
import com.gptlambda.api.ExecResultSync;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.SpecResult;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Biz Melesse created on 7/26/23
 */
public interface RuntimeService {

  /**
   * Results of user code execution for async access. This is primarily for development.
   * In production, we use FCM messaging to send the result to the client.
   * Results are purged after one consumption.
   */
  ConcurrentMap<String, ExecResultSync> executionResults = new ConcurrentHashMap<>();

  GenericResponse exec(ExecRequest execRequest);
  ExecResultSync getExecResult(String uid);
  String getUserCode(String uid);
  GenericResponse handleExecResult(ExecResultAsync execResult);
  String generateCodeVersion();
  CodeUpdateResponse updateCode(Code code);
  Code getCodeDetail(String uid);
  void generateOpenApiSpec(String interfaces, String uid);
  GenericResponse handleSpecResult(SpecResult specResult);
  GenericResponse deploy(ExecRequest execRequest);
}
