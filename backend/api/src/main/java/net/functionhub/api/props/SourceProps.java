package net.functionhub.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "source")
public class SourceProps {
    private String profile;
    private String name;
    private String baseUrl;
}
