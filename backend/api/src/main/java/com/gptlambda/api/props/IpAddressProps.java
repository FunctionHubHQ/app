package com.gptlambda.api.props;

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
@ConfigurationProperties(prefix = "ip-address")
public class IpAddressProps {
    private Set<String> whitelist = new HashSet<>();
    private Set<String> headers = new HashSet<>();
}
