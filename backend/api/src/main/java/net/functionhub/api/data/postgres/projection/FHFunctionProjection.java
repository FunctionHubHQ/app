package net.functionhub.api.data.postgres.projection;

import java.time.LocalDateTime;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface FHFunctionProjection {
  String getCodeid();
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
