package net.functionhub.api.data.postgres.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface FHFunctionProjection {
  UUID getCodeid();
  String getOwnerid();
  String getOwnerusername();
  String getOwneravatar();
  String getTags();
  String getSlug();
  Long getForkcount();
  Boolean getIspublic();
  String getSummary();
  String getDescription();
  String getName();
  LocalDateTime getCreatedat();
  LocalDateTime getUpdatedat();
}
