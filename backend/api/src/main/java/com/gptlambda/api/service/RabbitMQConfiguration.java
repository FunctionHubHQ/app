package com.gptlambda.api.service;

import com.gptlambda.api.props.RabbitMQProps;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * @author Biz Melesse created on 6/4/23
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfiguration {

  private final RabbitMQProps rabbitMQProps;

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitMQProps.getHost());
    connectionFactory.setUsername(rabbitMQProps.getUsername());
    connectionFactory.setPassword(rabbitMQProps.getPassword());
    return connectionFactory;
  }

  @Bean
  @Qualifier("scrape-job-queue")
  public Queue scrapeJobQueue(){
    return new Queue(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.SCRAPE_JOB_QUEUE_KEY)
        .getQueue());
  }

  @Bean
  @Qualifier("chat-job-queue")
  public Queue chatJobQueue(){
    return new Queue(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.CHAT_JOB_QUEUE_KEY)
        .getQueue());
  }

  @Bean
  @Qualifier("smart-proxy-job-queue")
  public Queue smartProxyJobQueue(){
    return new Queue(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.SMART_PROXY_JOB_QUEUE_KEY)
        .getQueue());
  }

  @Bean
  public TopicExchange exchange(){
    return new TopicExchange(rabbitMQProps.getTopicExchange());
  }


  @Bean
  public Binding scrapeJobBinding(@Qualifier("scrape-job-queue") Queue jobQueue){
    return BindingBuilder
        .bind(jobQueue)
        .to(exchange())
        .with(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.SCRAPE_JOB_QUEUE_KEY)
        .getRoutingKey());
  }

  @Bean
  public Binding smartProxyJobBinding(@Qualifier("smart-proxy-job-queue") Queue jobQueue){
    return BindingBuilder
        .bind(jobQueue)
        .to(exchange())
        .with(rabbitMQProps
            .getJobQueues()
            .get(RabbitMQProps.SMART_PROXY_JOB_QUEUE_KEY)
            .getRoutingKey());
  }

  @Bean
  public Binding chatJobBinding(@Qualifier("chat-job-queue") Queue jobQueue){
    return BindingBuilder
        .bind(jobQueue)
        .to(exchange())
        .with(rabbitMQProps
            .getJobQueues()
            .get(RabbitMQProps.CHAT_JOB_QUEUE_KEY)
            .getRoutingKey());
  }

  @Bean
  @Qualifier("scrape-job-listener-container")
  SimpleMessageListenerContainer scrapeJobListenerContainer(
      @Qualifier("scrape-job-message-listener") MessageListenerAdapter listenerAdapter,
      ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setConcurrentConsumers(rabbitMQProps.getConsumerConcurrency());
    container.setQueueNames(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.SCRAPE_JOB_QUEUE_KEY)
        .getQueue());
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  @Qualifier("chat-job-listener-container")
  SimpleMessageListenerContainer chatJobListenerContainer(
      @Qualifier("chat-job-message-listener") MessageListenerAdapter listenerAdapter,
      ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setConcurrentConsumers(rabbitMQProps.getConsumerConcurrency());
    container.setQueueNames(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.CHAT_JOB_QUEUE_KEY)
        .getQueue());
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  @Qualifier("smart-proxy-job-listener-container")
  SimpleMessageListenerContainer smartProxyJobListenerContainer(
      @Qualifier("smart-proxy-job-message-listener") MessageListenerAdapter listenerAdapter,
      ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setConcurrentConsumers(rabbitMQProps.getConsumerConcurrency());
    container.setQueueNames(rabbitMQProps
        .getJobQueues()
        .get(RabbitMQProps.SMART_PROXY_JOB_QUEUE_KEY)
        .getQueue());
    container.setMessageListener(listenerAdapter);
    return container;
  }

}
