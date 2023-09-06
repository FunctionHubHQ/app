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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.functionhub.api.UserProfile;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.dto.SessionUser;
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

  public static UserProfile getUserProfile() {
    SessionUser sessionUser = getSessionUser();
    return new UserProfile()
        .name(sessionUser.getName())
        .email(sessionUser.getEmail())
        .uid(sessionUser.getUid())
        .apiKey(sessionUser.getApiKey())
        .picture(sessionUser.getAvatar())
        .username(sessionUser.getUsername());
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
      user.setUid(projection.getUid());
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
}
