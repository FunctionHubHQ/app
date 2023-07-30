package com.gptlambda.api.props;

import com.gptlambda.api.dto.DenoInternal;
import com.gptlambda.api.dto.DenoRuntime;
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
    private DenoRuntime runtime;
    private DenoInternal internal;
}
