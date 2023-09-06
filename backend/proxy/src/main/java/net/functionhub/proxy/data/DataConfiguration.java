package net.functionhub.proxy.data;

import net.functionhub.proxy.data.redis.RedisConfiguration;
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
        RedisConfiguration.class
})
public class DataConfiguration {
}
