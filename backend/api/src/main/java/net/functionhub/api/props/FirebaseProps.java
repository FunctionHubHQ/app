package net.functionhub.api.props;

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
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProps {
    private String type;
    private String projectId;
    private String privateKeyId;
    private String privateKey;
    private String clientEmail;
    private String clientId;
    private String authUri;
    private String tokenUri;
    private String clientApiKey;
    private String customTokenVerificationUrl;
    private String passwordVerificationUrl;
    private String clientX509CertUrl;
    private String authProviderX509CertUrl;
}
