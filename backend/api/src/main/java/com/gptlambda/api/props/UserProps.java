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
 * created on 02/16/2023
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "user")
public class UserProps {
    private Set<String> adminEmails = new HashSet<>();

}
