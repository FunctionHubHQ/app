package net.functionhub.proxy.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Biz Melesse created on 8/29/23
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ProxyFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    filterChain.doFilter(request, response);
    // Extract proxy target and remove it from the rest of the user headers
    Enumeration<String> headers = request.getHeaderNames();
    Map<String, String> userHeaders = new HashMap<>();
    String fhProxyTarget = null;
    String fhApiKey = null;
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      String key = header.toLowerCase();
      if (key.equalsIgnoreCase("X-Function-Hub-Proxy-Target")) {
        fhProxyTarget = request.getHeader(header);
      } else if (key.equalsIgnoreCase("X-Function-Hub-Key")) {
        fhApiKey = request.getHeader(header);
      } else {
        userHeaders.put(header, request.getHeader(header));
      }
    }
    if (fhProxyTarget != null) {
      if (fhApiKey == null) {
        response.setStatus(HttpStatus.BAD_REQUEST_400);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("error", "Missing X-Function-Hub-Key header");
        objectMapper.writeValue(response.getWriter(), message);
        return;
      }
    }
    filterChain.doFilter(request, response);
  }
}
