package com.gptlambda.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptlambda.api.props.WebSocketProps;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author Biz Melesse created on 05/28/23
 * <a href="https://medium.com/javarevisited/building-persistable-one-to-one-chat-application-using-spring-boot-and-websockets-303ba5d30bb0">Based on this tutorial</a>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
  private final ObjectMapper objectMapper;
  private final WebSocketProps webSocketProps;
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Define in-memory message broker paths
    config.enableSimpleBroker(
        webSocketProps.getBroker(),
        webSocketProps.getAppDestination(),
        webSocketProps.getUserDestination());

    // Define message destination prefix as /shoppiem-app
    // This is the destination the client will be sending messages to
    config.setApplicationDestinationPrefixes(webSocketProps.getAppDestination());

    // Define user destination as /shoppiem-user. This is the destination
    // the client will be connecting to establish a WebSockets connection
    config.setUserDestinationPrefix(webSocketProps.getUserDestination());
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint(webSocketProps.getStompEndpoint())
        .setAllowedOriginPatterns("*");

    registry.addEndpoint(webSocketProps.getStompEndpoint())
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
    DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
    resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setObjectMapper(objectMapper);
    converter.setContentTypeResolver(resolver);
    messageConverters.add(converter);
    return false;
  }

}
