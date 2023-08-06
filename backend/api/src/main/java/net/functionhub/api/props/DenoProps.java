package net.functionhub.api.props;

import net.functionhub.api.dto.DenoInstance;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 07/30/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "deno")
public class DenoProps {
    private DenoInstance runtime;
    private DenoInstance internal;
}
