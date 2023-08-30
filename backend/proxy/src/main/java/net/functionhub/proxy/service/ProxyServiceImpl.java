package net.functionhub.proxy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.proxy.dto.UserHttpRequest;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 8/30/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {
  private final HttpServletRequest httpServletRequest;
  private final HttpServletResponse httpServletResponse;
  private final ObjectMapper objectMapper;

  @Override
  public void handler() throws IOException {
    UserHttpRequest userHttpRequest = getHeaders();
    if (ObjectUtils.isEmpty(userHttpRequest) ||
        ObjectUtils.isEmpty(userHttpRequest.getHeaders())) {
      httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
      Map<String, String> message = new HashMap<>();
      message.put("error", "Encountered an unknown error");
      objectMapper.writeValue(httpServletResponse.getWriter(), message);
    } else {
      if (ObjectUtils.isEmpty(userHttpRequest.getProxyTarget())) {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("error", "URL cannot be empty"); // This should really be handled by the http client
        objectMapper.writeValue(httpServletResponse.getWriter(), message);
      } else if (ObjectUtils.isEmpty(userHttpRequest.getFhApiKey())) {
        httpServletResponse.setStatus(HttpStatus.BAD_REQUEST_400);
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("error", "Missing X-Function-Hub-Key header");
        objectMapper.writeValue(httpServletResponse.getWriter(), message);
      } else {
        httpServletResponse.setStatus(HttpStatus.OK_200);
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("message", "Proxy request received");
        message.put("X-Function-Hub-Proxy-Target", userHttpRequest.getProxyTarget());
        message.put("X-Function-Hub-Key", userHttpRequest.getFhApiKey());
        objectMapper.writeValue(httpServletResponse.getWriter(), message);
      }
    }
  }

  @Override
  public UserHttpRequest getHeaders() throws IOException {
    UserHttpRequest userHttpRequest = new UserHttpRequest();
    // Extract proxy target and remove it from the rest of the user headers
    Enumeration<String> headers = httpServletRequest.getHeaderNames();
    Map<String, String> rawHeaders = new HashMap<>();
    String fhProxyTarget = null;
    String fhApiKey = null;
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      String key = header.toLowerCase();
      if (key.equalsIgnoreCase("X-Function-Hub-Proxy-Target")) {
        fhProxyTarget = httpServletRequest.getHeader(header);
      } else if (key.equalsIgnoreCase("X-Function-Hub-Key")) {
        fhApiKey = httpServletRequest.getHeader(header);
      } else {
        rawHeaders.put(header, httpServletRequest.getHeader(header));
      }
    }
    userHttpRequest.setHeaders(rawHeaders);
    userHttpRequest.setProxyTarget(fhProxyTarget);
    userHttpRequest.setFhApiKey(fhApiKey);

    if (fhProxyTarget != null) {
      if (fhApiKey == null) {
        httpServletResponse.setStatus(HttpStatus.BAD_REQUEST_400);
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("error", "Missing X-Function-Hub-Key header");
        objectMapper.writeValue(httpServletResponse.getWriter(), message);
        return null;
      }
    }
    return userHttpRequest;
  }
}
