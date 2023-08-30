package net.functionhub.proxy.data;


import net.functionhub.proxy.data.postgres.PostgresDBDataConfiguration;
import net.functionhub.proxy.props.PropConfiguration;
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
@Import({
        PostgresDBDataConfiguration.class,
        PropConfiguration.class
})
public class DataConfiguration {
}
