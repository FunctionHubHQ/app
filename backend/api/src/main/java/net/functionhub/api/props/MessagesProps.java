package net.functionhub.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 09/03/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "messages")
public class MessagesProps {
    private String invocationLimitReached;
    private String dataTransferLimitReached;
    private String unauthorized;
    private String noReturnValue;
    private String executionTimeout;
    private String serviceNotFound;
    private String resultProcessingError;
    private String forkToExec;
    private String signInForkToExec;

}
