package net.functionhub.api.data;


import net.functionhub.api.data.postgres.PostgresDBDataConfiguration;
import net.functionhub.api.data.redis.RedisConfiguration;
import net.functionhub.api.props.PropConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude = {
    FlywayAutoConfiguration.class
})
@Import({
    PostgresDBDataConfiguration.class,
    PropConfiguration.class,
    RedisConfiguration.class
})
public class DataConfiguration {
}
