package com.gptlambda.api.utils.security;

import com.gptlambda.api.dto.RequestHeaders;
import com.gptlambda.api.service.user.UserService;
import com.gptlambda.api.utils.security.firebase.FirebaseSecurityConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Configuration
@Import({
        FirebaseSecurityConfiguration.class
})
@RequiredArgsConstructor
public class SecurityConfiguration {

  @Bean
  @Primary
  @RequestScope
  public RequestHeaders requestHeader(HttpServletRequest httpServletRequest) {
    // Normalize the headers by lower-casing all the keys
    Map<String, String> normalized = new HashMap<>();
    Enumeration<String> headers = httpServletRequest.getHeaderNames();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      normalized.put(header.toLowerCase(), httpServletRequest.getHeader(header));
      String key = header.toLowerCase();
      if (key.equals("authorization")) {
        String bearer = normalized.get(key).substring(7);
        if (bearer.startsWith(UserService.apiKeyPrefix)) {
          normalized.put("api-key", bearer);
        }
      }
    }
    return new RequestHeaders(normalized);
  }
}
