package com.gptlambda.api.service;

import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.service.openai.OpenAiService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Biz Melesse created on 6/13/23
 */
@Configuration
@RequiredArgsConstructor
public class OpenAIConfiguration {
  private final OpenAiProps openAiProps;

  @Bean
  public OpenAiService openAiService() {
    return new OpenAiService(openAiProps.getApiKey(),
        Duration.ofSeconds(openAiProps.getRequestTimeoutSeconds()));
  }
}
