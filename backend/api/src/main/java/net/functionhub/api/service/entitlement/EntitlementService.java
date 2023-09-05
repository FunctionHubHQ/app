package net.functionhub.api.service.entitlement;

/**
 * @author Biz Melesse created on 9/3/23
 */
public interface EntitlementService {
  String INVOCATION_KEY_PREFIX = "func_invocation:";
  void recordFunctionInvocation();
  long getNumFunctionInvocations(int lastNMinutes);

  /**
   * Create an execution session with a 5-minute expiration. The access
   * token needs to be set as X-FH-Access-Token header on all outgoing
   * http requests from user code. Then the proxy server will use the
   * token to check entitlements and apply the appropriate limits. All
   * the necessary information is encoded in this execution-specific
   * access token. Note that we don't remove the session manually. Setting
   * an expiration should take care of that.
   * @param accessToken
   */
  void createExecutionSession(String accessToken);
}
