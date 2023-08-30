package net.functionhub.proxy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse created on 8/30/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProxyController {
  private final HttpServletRequest httpServletRequest;
  private final HttpServletResponse httpServletResponse;
  private final ObjectMapper objectMapper;

  @RequestMapping(
      value = "/proxy",
      method = {
          RequestMethod.GET,
          RequestMethod.POST,
          RequestMethod.PUT,
          RequestMethod.HEAD,
          RequestMethod.DELETE,
          RequestMethod.OPTIONS,
          RequestMethod.PATCH,
          RequestMethod.TRACE})
  public void proxyHandler() throws IOException {
    httpServletResponse.setStatus(HttpStatus.OK_200);
    httpServletResponse.setContentType("application/json");
    Map<String, String> message = new HashMap<>();
    message.put("message", "Proxy request received");
    objectMapper.writeValue(httpServletResponse.getWriter(), message);
  }
}
