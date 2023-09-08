package net.functionhub.api.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.functionhub.api.Project;
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
  private Long maxExecutionTime;
  private Long maxCpuTime;
  private Long maxMemoryUsage;
  private Long maxDataTransfer;
  private Long maxHttpCalls;
  private Long maxInvocations;
  private Long maxFunctions;
  private Long maxProjects;
  private AuthMode authMode;
  private List<Project> projects;
  private Map<String, Boolean> roles = new HashMap<>();
}
