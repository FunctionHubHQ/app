package net.functionhub.api.service.runtime;

import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResponse;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.ExecResultAsync;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.SpecResult;
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
  ConcurrentMap<String, ExecResultAsync> executionResults = new ConcurrentHashMap<>();

  /**
   * For dev/test use due to transaction issues
   */
  ConcurrentMap<String, String> jsonSchema = new ConcurrentHashMap<>();

  ExecResultAsync getExecutionResult(String executionId);
  GenericResponse exec(ExecRequest execRequest);
  String getUserCode(String uid);
  GenericResponse handleExecResult(ExecResultAsync execResult);
  String generateCodeVersion();
  CodeUpdateResponse updateCode(Code code);
  Code getCodeDetail(String uid);
  void generateJsonSchema(String interfaces, String uid);
  String getJsonSchema(String uid);
  GenericResponse handleSpecResult(SpecResult specResult);
  GenericResponse deploy(ExecRequest execRequest);
}
