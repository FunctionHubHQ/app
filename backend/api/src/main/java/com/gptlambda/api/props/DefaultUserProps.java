package com.gptlambda.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "user")
public class DefaultUserProps {
    private List<String> claims;
}
