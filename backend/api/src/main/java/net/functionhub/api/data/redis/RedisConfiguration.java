package net.functionhub.api.data.redis;

import lombok.RequiredArgsConstructor;
import net.functionhub.api.props.RedisProps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Biz Melesse created on 9/3/23
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {
  private final RedisProps redisProps;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
    configuration.setHostName(redisProps.getHost());
    configuration.setPassword(redisProps.getPassword());
    configuration.setDatabase(redisProps.getDatabase());
    configuration.setPort(redisProps.getPort());
    return new LettuceConnectionFactory(configuration);

  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    return redisTemplate;
  }
}