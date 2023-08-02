package com.gptlambda.api.data.postgres.projection;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface Deployment {
  String getCodeId();
  String getJsonSchema();
  String getFunctionName();
  String getDescription();
}
