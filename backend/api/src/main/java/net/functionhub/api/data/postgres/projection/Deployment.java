package net.functionhub.api.data.postgres.projection;

import java.time.LocalDateTime;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface Deployment {
  String getId();
  String getOwnerid();
  String getPayload();
  String getName();
  String getDescription();
  String getVersion();
  String getSchema();
  LocalDateTime getCreatedat();
}
