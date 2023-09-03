package net.functionhub.api.service.entitlement;

/**
 * @author Biz Melesse created on 9/3/23
 */
public interface EntitlementService {
  String INVOCATION_KEY_PREFIX = "func_invocation:";
  void recordFunctionInvocation();
  long getNumFunctionInvocations(int lastNMinutes);
}
