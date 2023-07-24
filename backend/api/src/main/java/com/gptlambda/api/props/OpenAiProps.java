package com.gptlambda.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 1/31/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiProps {
    private int numRetries = 5;
    private Boolean useQueryGenerator = false;
    private String apiKey;
    private String queryBuilderPrompt;
    private String embeddingModel = "text-embedding-ada-002";
    private String completionModel = "gpt-3.5-turbo";
    private String systemMessage;
    private long requestTimeoutSeconds = 120;
    private double temp = 0.7;
    private int maxTokens = 1000;
    private String completionEndpoint = "https://api.openai.com/v1/chat/completions";
}
