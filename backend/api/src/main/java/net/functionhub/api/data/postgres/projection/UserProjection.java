package net.functionhub.api.data.postgres.projection;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface UserProjection {
  String getEmail();
  String getName();
  String getUid();
  String getAvatar();
  String getApikey();
  String getUsername();
  Long getMaxexecutiontime();
  Long getMaxcputtime();
  Long getMaxmemoryusage();
  Long getMaxbandwidth();
  Long getNumhttpcalls();
  Long getNuminvocations();
  Long getNumfunctions();
  Long getNumprojects();
}
