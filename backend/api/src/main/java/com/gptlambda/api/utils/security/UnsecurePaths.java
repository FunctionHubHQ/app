package com.gptlambda.api.utils.security;

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
  private final List<String> paths = new ArrayList<>(List.of(
      "/actuator/health",
      "/actuator/health/**"
  ));

  public boolean allow(String path) {
    return paths.stream()
        .anyMatch(uri -> path.contains(uri.toLowerCase()));
  }

  public String[] wildcardPaths() {
    return paths.stream().filter(p -> p.endsWith("**"))
        .toArray(String[]::new);
  }
}
