package net.functionhub.api.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 5/11/22
 */
@Data
@Configuration
@Primary
@ConfigurationProperties("jwt")
public class JwtProps {
    private String secret;
    private Long ttlDays;
}
