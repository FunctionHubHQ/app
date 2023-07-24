package com.gptlambda.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AWSProps {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private String videoFormat;
    private String cdn;
    private String videoFolder;
    private String audioFolder;
    private String imageFolder;
    private String streamFolder;
}
