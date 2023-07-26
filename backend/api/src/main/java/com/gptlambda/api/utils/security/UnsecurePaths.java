package com.gptlambda.api.utils.security;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Biz Melesse created on 5/28/23
 */
public class UnsecurePaths {
  private static final List<String> paths = List.of(
      "/actuator/health",
      "/actuator/health/**",
      "/npm",
      "/npm/**",
      "/run",
      "/run/**"
  );

  public static Stream<String> paths() {
    return paths.stream();
  }

  public static String[] wildcardPaths() {
    return paths.stream().filter(p -> p.endsWith("**"))
        .toArray(String[]::new);
  }

}
