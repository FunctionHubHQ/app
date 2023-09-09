package net.functionhub.api.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.functionhub.api.Project;
import net.functionhub.api.UserProfile;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.projection.Deployment;
import net.functionhub.api.data.postgres.projection.UserProjectProjection;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.dto.SessionUser;
import net.functionhub.api.service.user.UserService;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ResourceUtils;

/**
 * @author Biz Melesse created on 1/2/23
 */
public class FHUtils {
  public static final int SHORT_UID_LENGTH = 8;
  public static final int LONG_UID_LENGTH = 16;
  public static final int API_KEY_LENGTH = 46;

  public static String generateUid(int length) {
    StringBuilder builder = new StringBuilder();
    // An ID length of N gives 62^N unique IDs
    for (int i = 0; i < length; i++) {
      builder.append(getRandomCharacter());
    }
    return builder.toString();
  }

  public static Character getRandomCharacter() {
    Random random = new Random();
    String uidAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoqprstuvwxyz0123456789";
    int index = random.nextInt(uidAlphabet.length());
    return uidAlphabet.charAt(index);
  }

  public static String truncate(String value) {
    int maxLength = 255;
    if (value.length() > maxLength) {
      return value.substring(0, maxLength);
    }
    return value;
  }

  public static String loadFile(String path) {
    File file = null;
    try {
      file = ResourceUtils.getFile("classpath:" + path);
      try {
        return new String(Files.readAllBytes(file.toPath()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getCurrentTime() {
    return LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  public static SessionUser getSessionUser() {
    try {
      return (SessionUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    } catch (Exception e) {
      return new SessionUser();
    }
  }

  public static UserProfile getUserProfile(List<UserProjectProjection> projections) {
    SessionUser sessionUser = getSessionUser();
    return new UserProfile()
        .name(sessionUser.getName())
        .email(sessionUser.getEmail())
        .userId(sessionUser.getUserId())
        .apiKey(sessionUser.getApiKey())
        .picture(sessionUser.getAvatar())
        .username(sessionUser.getUsername())
        .anonymous(sessionUser.isAnonymous())
        .projects(getUserProjects(projections, sessionUser.getMaxFunctions()));
  }

  public static List<Project> getUserProjects(List<UserProjectProjection> projections, Long maxFunctionsAllowed) {
    // lambda doesn't propagate errors so just loop over it
    List<Project> projects = new ArrayList<>();
    for (UserProjectProjection it : projections) {
      projects.add(new Project()
          .projectId(it.getProjectid())
          .updatedAt(it.getUpdatedat().toEpochSecond(ZoneOffset.UTC))
          .createdAt(it.getCreatedat().toEpochSecond(ZoneOffset.UTC))
          .name(it.getProjectname())
          .description(it.getDescription())
          .numFunctions(it.getNumfunctions())
          .full(maxFunctionsAllowed == null || it.getNumfunctions() >= maxFunctionsAllowed));
    }
    return projects;
  }

  public static String raiseHttpError(HttpServletResponse httpServletResponse,
      ObjectMapper objectMapper,
      String msg, int errorCode) {
    raiseHttpErrorHelper(httpServletResponse, objectMapper, msg, errorCode);
    return null;
  }

  public static void raiseHttpErrorHelper(HttpServletResponse httpServletResponse,
      ObjectMapper objectMapper,
      String msg, int errorCode) {
    httpServletResponse.setStatus(errorCode);
    httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
    Map<String, String> message = new HashMap<>();
    message.put("error", msg);
    try {
      objectMapper.writeValue(httpServletResponse.getWriter(), message);
    } catch (IOException e) {
      httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  public static void populateSessionUser(UserProjection projection, SessionUser user) {
    if (projection != null) {
      user.setEmail(projection.getEmail());
      user.setName(projection.getName());
      user.setUserId(projection.getUserid());
      user.setAvatar(projection.getAvatar());
      user.setApiKey(projection.getApikey());
      user.setUsername(projection.getUsername());
      user.setMaxExecutionTime(projection.getMaxexecutiontime());
      user.setMaxCpuTime(projection.getMaxcputtime());
      user.setMaxMemoryUsage(projection.getMaxmemoryusage());
      user.setMaxDataTransfer(projection.getMaxdatatransfer());
      user.setMaxHttpCalls(projection.getMaxhttpcalls());
      user.setMaxInvocations(projection.getMaxinvocations());
      user.setMaxFunctions(projection.getMaxfunctions());
      user.setMaxProjects(projection.getMaxprojects());
    }
  }

  public static void populateAnonymousUser(SessionUser user) {
    if (user != null) {
      user.setName("Anonymous");
      user.setAnonymous(true);
      user.setEmail("");
      user.setUserId(generateUid(SHORT_UID_LENGTH));
      user.setAvatar("");
      user.setApiKey(user.getApiKey() == null ? UserService.apiKeyPrefix + "anon-" + generateUid(API_KEY_LENGTH) : user.getApiKey());
      user.setUsername("");
      user.setMaxExecutionTime(0L);
      user.setMaxCpuTime(0L);
      user.setMaxMemoryUsage(0L);
      user.setMaxDataTransfer(0L);
      user.setMaxHttpCalls(0L);
      user.setMaxInvocations(0L);
      user.setMaxFunctions(0L);
      user.setMaxProjects(0L);
    }
  }

  public static boolean hasExecAccess(CodeCellEntity codeCell, HttpServletResponse httpServletResponse,
      ObjectMapper objectMapper, String message) {
    return hasWriteAccess(codeCell, httpServletResponse, objectMapper, message);
  }

  public static boolean hasWriteAccess(CodeCellEntity codeCell,
      HttpServletResponse httpServletResponse, ObjectMapper objectMapper,
      String message) {
    boolean hasAccess = false;
    if (codeCell != null) {
      hasAccess = codeCell.getUserId().equals(FHUtils.getSessionUser().getUserId());
    }
    if (!hasAccess) {
      FHUtils.raiseHttpError(httpServletResponse,
          objectMapper,
          message,
          HttpStatus.FORBIDDEN_403);
    }
    return hasAccess;
  }

  public static boolean hasReadAccess(CodeCellEntity codeCell,
      HttpServletResponse httpServletResponse, ObjectMapper objectMapper,
      String message) {
    boolean hasAccess = false;
    if (codeCell != null) {
      if (codeCell.getIsPublic() != null && codeCell.getIsPublic()) {
        hasAccess = true;
      } else {
        hasAccess = codeCell.getUserId().equals(FHUtils.getSessionUser().getUserId());
      }
    }
    if (!hasAccess) {
      FHUtils.raiseHttpError(httpServletResponse,
          objectMapper,
          message,
          HttpStatus.FORBIDDEN_403);
    }
    return hasAccess;
  }

  public static boolean hasReadAccess(Deployment deployment,
      HttpServletResponse httpServletResponse, ObjectMapper objectMapper,
      String message) {
    boolean hasAccess = false;
    if (deployment != null) {
      hasAccess = deployment.getOwnerid().equals(FHUtils.getSessionUser().getUserId());
    }
    if (!hasAccess) {
      FHUtils.raiseHttpError(httpServletResponse,
          objectMapper,
          message,
          HttpStatus.FORBIDDEN_403);
    }
    return hasAccess;
  }

  public static String generateEntityId(String prefix) {
    return prefix + generateUid(SHORT_UID_LENGTH);
  }
}
