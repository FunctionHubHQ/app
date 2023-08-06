package net.functionhub.api.service;

import net.functionhub.api.service.openai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Biz Melesse created on 6/13/23
 */
@Configuration
@RequiredArgsConstructor
public class OpenAIConfiguration {

  @Bean
  public OpenAiService openAiService() {
//    return new OpenAiService(openAiProps.getApiKey(),
//        Duration.ofSeconds(openAiProps.getRequestTimeoutSeconds()));
    return null;
  }
}
