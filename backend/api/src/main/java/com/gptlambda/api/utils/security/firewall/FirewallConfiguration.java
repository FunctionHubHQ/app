package com.gptlambda.api.utils.security.firewall;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * @author Bizuwork Melesse
 * created on 2/13/22
 */
@Configuration
public class FirewallConfiguration {

    @Bean
    public HttpFirewall firewallOverride() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowNull(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setUnsafeAllowAnyHttpMethod(true);
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }
}
