package net.functionhub.proxy.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 09/03/23
 *
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties("api")
public class ApiProps {
    private String logUrl;
    private String apiKey;
}
