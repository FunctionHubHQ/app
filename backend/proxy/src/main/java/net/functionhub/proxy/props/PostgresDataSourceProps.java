package net.functionhub.proxy.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties("spring.datasource")
public class PostgresDataSourceProps {
    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
