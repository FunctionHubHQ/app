package net.functionhub.api.utils.security;

import net.functionhub.api.props.CorsProps;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 5/28/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnsecurePaths {
  private final CorsProps corsProps;

  private final List<String> paths = new ArrayList<>(List.of(
      "/actuator/health",
      "/actuator/health/**",
      "/npm",
      "/npm/**"
  ));

  public boolean allow(String path) {
    return paths.stream()
        .anyMatch(uri -> path.contains(uri.toLowerCase()));
  }

  public String[] wildcardPaths() {
    return paths.stream().filter(p -> p.endsWith("**"))
        .toArray(String[]::new);
  }

  public boolean allowedOrigin(String remoteAddr, String path) {
    return corsProps.getAllowedOriginPatterns().contains(remoteAddr) &&
        paths.contains(path);
  }
}
