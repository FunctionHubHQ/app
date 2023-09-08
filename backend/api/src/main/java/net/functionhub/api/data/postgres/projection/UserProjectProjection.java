package net.functionhub.api.data.postgres.projection;

import java.time.LocalDateTime;

/**
 * @author Biz Melesse created on 9/7/23
 */
public interface UserProjectProjection {
  String getProjectid();
  String getProjectname();
  String getDescription();
  LocalDateTime getUpdatedat();
  LocalDateTime getCreatedat();
  Long getNumfunctions();
}
