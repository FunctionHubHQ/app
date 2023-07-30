package com.gptlambda.api.utils.security;

import com.gptlambda.api.props.SourceProps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author Biz Melesse created on 5/28/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnsecurePaths implements ApplicationListener<ContextRefreshedEvent> {
  private final SourceProps sourceProps;
  private final List<String> paths = new ArrayList<>(List.of(
      "/actuator/health",
      "/actuator/health/**",
      "/npm",
      "/npm/**",
      "/run",
      "/run/**",
      "/e-result",
      "/e-result/**",
      "/s-result",
      "/s-result/**",
      "/update-code",
      "/update-code/**",
      "/deploy",
      "/deploy/**"
  ));


  public boolean allow(String path) {
    if (sourceProps.getProfile().equals("test")) {
      return true;
    }
    return paths.stream()
        .anyMatch(uri -> path.contains(uri.toLowerCase()));
  }

  public String[] wildcardPaths() {
    if (sourceProps.getProfile().equals("test")) {
      return new String[]{"/**"};
    }
    return paths.stream().filter(p -> p.endsWith("**"))
        .toArray(String[]::new);
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (sourceProps.getProfile().equals("test")) {
      paths.clear();
      ApplicationContext applicationContext = event.getApplicationContext();
      RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
          .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
      Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping
          .getHandlerMethods();
      map.forEach((key, value) -> {
        List<String> potentialPaths = key.getDirectPaths()
            .stream()
            .toList();
        if (!ObjectUtils.isEmpty(potentialPaths)) {
          String path = Stream.of(
              potentialPaths.get(0)
              .split("/")
          ).filter(it -> !ObjectUtils.isEmpty(it))
              .toList().get(0);
          paths.add("/" + path + "/**");
        }
      });
    }
  }
}
