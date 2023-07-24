package com.gptlambda.api.data;


import com.gptlambda.api.data.postgres.PostgresDBDataConfiguration;
import com.gptlambda.api.props.PropConfiguration;
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
