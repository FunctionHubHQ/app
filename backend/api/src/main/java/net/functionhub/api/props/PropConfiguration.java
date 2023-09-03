package net.functionhub.api.props;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(value = {
    FirebaseProps.class,
    FlywayPostgresProps.class,
    PostgresDataSourceProps.class,
    SourceProps.class,
    DefaultUserProps.class,
    UserProps.class,
    RabbitMQProps.class,
    CorsProps.class,
    DenoProps.class,
    OpenAiProps.class,
    EntitlementProps.class,
    JwtProps.class,
    DefaultConfigsProps.class
})
public class PropConfiguration {
}
