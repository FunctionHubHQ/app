package net.functionhub.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 08/21/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "spring.flyway.postgres")
public class FlywayPostgresProps {
    private String schemas;
    private String locations;
    private Boolean cleanDisabled = true;
}
