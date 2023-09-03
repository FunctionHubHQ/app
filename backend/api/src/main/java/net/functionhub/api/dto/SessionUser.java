package net.functionhub.api.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.functionhub.api.service.user.UserService.AuthMode;

/**
 * @author Biz Melesse created on 9/3/23
 */
@Getter @Setter
public class SessionUser {
  private String email;
  private String name;
  private String uid;
  private String avatar;
  private String apiKey;
  private String username;
  private String maxExecutionTime;
  private String maxCpuTime;
  private String maxMemoryUsage;
  private String maxBandwidth;
  private String numHttpCalls;
  private String numInvocations;
  private String numFunctions;
  private String numProjects;
  private AuthMode authMode;
  private Map<String, Boolean> roles;
}
