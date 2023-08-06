package net.functionhub.api.props;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 2/18/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "cors")
public class CorsProps {
    private Set<String> allowedOriginPatterns = new HashSet<>();
}
