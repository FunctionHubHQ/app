package net.functionhub.api.props;

import net.functionhub.api.dto.JobQueue;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 06/05/2023
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProps {
    public static final String SCRAPE_JOB_QUEUE_KEY = "scraping";
    public static final String CHAT_JOB_QUEUE_KEY = "chat";
    public static final String SMART_PROXY_JOB_QUEUE_KEY = "smart-proxy";

    private String host;
    private int port;
    private String username;
    private String password;
    private int consumerConcurrency;
    private String topicExchange;
    Map<String, JobQueue> jobQueues;
}
