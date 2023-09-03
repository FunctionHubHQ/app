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
  String getMaxexecutiontime();
  String getMaxcputtime();
  String getMaxmemoryusage();
  String getMaxbandwidth();
  String getNumhttpcalls();
  String getNuminvocations();
  String getNumfunctions();
  String getNumprojects();
}
