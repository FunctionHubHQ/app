package com.gptlambda.api.data.postgres.projection;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface Deployment {
  String getId();
  String getPayload();
  String getName();
  String getDescription();
  String getVersion();
}
