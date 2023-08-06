package net.functionhub.api.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.functionhub.api.props.RabbitMQProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 7/2/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobUtils {
  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMQProps rabbitMQProps;
//  private final ScraperProps scraperProps;

//  public void submitJobs(List<ScrapingJob> jobs, String routingPrefix) {
//    for (ScrapingJob job : jobs) {
//      try {
//        String jobString = objectMapper.writeValueAsString(job);
//        rabbitTemplate.convertAndSend(
//            rabbitMQProps.getTopicExchange(), routingPrefix + job.getProductSku(), jobString);
//      } catch (JsonProcessingException e) {
//        log.error(e.getLocalizedMessage());
//      }
//    }
//  }

//  public void submitJob(SmartProxyJob job, String routingPrefix) {
//    try {
//      String jobString = objectMapper.writeValueAsString(job);
//      rabbitTemplate.convertAndSend(
//          rabbitMQProps.getTopicExchange(), routingPrefix + job.getProductSku(), jobString);
//    } catch (JsonProcessingException e) {
//      log.error(e.getLocalizedMessage());
//    }
//  }


}
