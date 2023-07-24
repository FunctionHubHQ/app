package com.gptlambda.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 05/29/2023
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "scraper")
public class ScraperProps {
    private boolean useSmartProxy;
    private boolean saveHtml;
    private long throttleMin;
    private long throttleMax;
}
