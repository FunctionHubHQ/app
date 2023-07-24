package com.gptlambda.api.utils.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * @author Bizuwork Melesse
 * created on 2/25/22
 */
@Configuration
public class ApplicationContextConfiguration implements EnvironmentPostProcessor {

  @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Map<String, Object> propertyOverrides = new LinkedHashMap<>();
    String MR_SSH_KEY = "MR_SSH_KEY";
    String MR_SSH_KEY_B64 = "MR_SSH_KEY_B64";
    propertyOverrides.put(MR_SSH_KEY, new String(Base64.getDecoder().decode(System.getenv(MR_SSH_KEY_B64)), StandardCharsets.UTF_8));
        environment.getPropertySources().addBefore(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, new MapPropertySource("decoded", propertyOverrides));
  }
}
