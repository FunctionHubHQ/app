package net.functionhub.api.props;

import lombok.Getter;
import lombok.Setter;
import net.functionhub.api.dto.Entitlement;
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
@ConfigurationProperties(prefix = "entitlement")
public class EntitlementProps {
    private Entitlement free;
    private Entitlement paid;
}
