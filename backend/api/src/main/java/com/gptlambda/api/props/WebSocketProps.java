package com.gptlambda.api.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Data
@Configuration
@Primary
@ConfigurationProperties("websocket")
public class WebSocketProps {
    private String broker = "/shoppiem";
    private String appDestination = "/shoppiem-app";
    private String userDestination =  "/shoppiem-user";
    private String queue = "/queue/messages";
    private String stompEndpoint = "/ws";
}
