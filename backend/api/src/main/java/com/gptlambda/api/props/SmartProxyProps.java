package com.gptlambda.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 6/10/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "smart-proxy")
public class SmartProxyProps {
    private String requestUrl;
    private String token;
    private String callbackUrl;
    private String resultUrl;
}
