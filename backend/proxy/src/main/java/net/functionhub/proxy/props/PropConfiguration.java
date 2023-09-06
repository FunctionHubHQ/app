package net.functionhub.proxy.props;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Bizuwork Melesse
 * created on 8/29/23
 *
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(value = {
    RedisProps.class
})
public class PropConfiguration {
}
