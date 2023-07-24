package com.gptlambda.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 7/13/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "other")
public class OtherProps {
    private String chromeExtensionStoreUrl;
    private Long lastNHistoryMessages = 25L;
}
