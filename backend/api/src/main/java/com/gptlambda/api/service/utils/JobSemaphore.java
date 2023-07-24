package com.gptlambda.api.service.utils;

import com.gptlambda.api.props.RabbitMQProps;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Biz Melesse created on 6/6/23
 */
@Component
@RequiredArgsConstructor
public class JobSemaphore {

  /**
   * The number of concurrent scraping requests must not exceed a certain
   * number. This number depends on the subscription plan we have for proxy
   * rotation and how many scraping threads they recommend. We need to block
   * the consumer until a slot becomes available.
   */
  private final RabbitMQProps rabbitMQProps;

  private Semaphore scrapeJobSemaphore = null;
  private Semaphore chatJobSemaphore = null;
  private Semaphore smartProxyJobSemaphore = null;


  public Semaphore getScrapeJobSemaphore() {
    if (scrapeJobSemaphore == null) {
      scrapeJobSemaphore = new Semaphore(rabbitMQProps
          .getJobQueues()
          .get(RabbitMQProps.SCRAPE_JOB_QUEUE_KEY)
          .getThreadCount());
    }
    return scrapeJobSemaphore;
  }

  public Semaphore getChatJobSemaphore() {
    if (chatJobSemaphore == null) {
      chatJobSemaphore = new Semaphore(rabbitMQProps
          .getJobQueues()
          .get(RabbitMQProps.CHAT_JOB_QUEUE_KEY)
          .getThreadCount());
    }
    return chatJobSemaphore;
  }

  public Semaphore getSmartProxyJobSemaphore() {
    if (smartProxyJobSemaphore == null) {
      smartProxyJobSemaphore = new Semaphore(rabbitMQProps
          .getJobQueues()
          .get(RabbitMQProps.SCRAPE_JOB_QUEUE_KEY)
          .getThreadCount());
    }
    return smartProxyJobSemaphore;
  }

}
