package net.functionhub.api.service.runtime;

import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.ExecResultAsync;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.SpecResult;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.functionhub.api.StatusRequest;
import net.functionhub.api.StatusResponse;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.dto.SessionUser;

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

  /**
   * Execute user function. Mainly used internally for testing purposes.
   * @param execRequest
   * @param applyEntitlementLimits whether entitlement limits should be applied
   * @return
   */
  ExecResultAsync exec(ExecRequest execRequest, boolean applyEntitlementLimits);

  /**
   * Execute deployed user function. Apply any prod entitlements and
   * basic function security. Require API key auth mode.
   * @param functionSlug
   * @param body
   * @return
   */
  String runProdFunction(String functionSlug, String body);

  /**
   * Execute user function in dev mode. Require Firebase auth mode.
   * @param functionSlug
   * @param body
   * @return
   */
  String runDevFunction(String functionSlug, String body);

  String getUserCode(String uid);
  GenericResponse handleExecResult(ExecResultAsync execResult);
  CodeUpdateResult updateCode(Code code, boolean forked, boolean initialUserFunction);
  Code getCodeDetail(String uid, Boolean bySlug);
  void generateJsonSchema(CodeCellEntity codeCell);
  String getJsonSchema(String uid);
  GenericResponse handleSpecResult(SpecResult specResult);
  GenericResponse deploy(ExecRequest execRequest);

  /**
   * Get user spec for rendering in Rapidoc. This endpoint is called by the Rapidoc UI
   * when the spec url is set.
   * @param functionId
   * @param version
   * @param env
   * @return
   */
  String getUserSpec(String functionId, String version, String env);

  /**
   * Get the status of OpenApi spec. Spec generation is done asynchronously
   * so the client must poll this endpoint before rendering the Rapidoc UI.
   * @param statusRequest
   * @return
   */
  StatusResponse getSpecStatus(StatusRequest statusRequest);

  /**
   * Generate an access token for authorizing and tracking user invocations.
   *
   * @return
   */
  String generateProxyAccessToken(String executionId);
}
