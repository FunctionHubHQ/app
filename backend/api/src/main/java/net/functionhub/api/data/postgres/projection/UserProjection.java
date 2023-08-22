package net.functionhub.api.data.postgres.projection;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface UserProjection {
  String getEmail();
  String getName();
  String getUid();
  String getAvatar();
  String getApiKey();
  String getUsername();
}
