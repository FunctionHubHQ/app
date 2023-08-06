package net.functionhub.api.service;


import net.functionhub.api.data.DataConfiguration;
import net.functionhub.api.service.mapper.MapperConfiguration;
import net.functionhub.api.service.messaging.Consumer;
import net.functionhub.api.utils.migration.FlywayMigrationConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Slf4j
@Configuration
@ComponentScan
@RequiredArgsConstructor
@EnableAsync
@Import({
    RabbitMQConfiguration.class,
    DataConfiguration.class,
    MapperConfiguration.class,
    OpenAIConfiguration.class,
    FlywayMigrationConfiguration.class})
public class ServiceConfiguration {
  private final Consumer consumer;

  @Bean
  @Qualifier("scrape-job-message-listener")
  public MessageListenerAdapter scrapeJobConsumer() {
    return new MessageListenerAdapter(consumer, "scrapeJobConsumer");
  }

  @Bean
  @Qualifier("chat-job-message-listener")
  public MessageListenerAdapter chatJobConsumer() {
    return new MessageListenerAdapter(consumer, "chatJobConsumer");
  }

  @Bean
  @Qualifier("smart-proxy-job-message-listener")
  public MessageListenerAdapter smartProxyJobConsumer() {
    return new MessageListenerAdapter(consumer, "smartProxyJobConsumer");
  }
}
