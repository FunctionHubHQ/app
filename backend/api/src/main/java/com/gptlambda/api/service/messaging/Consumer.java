package com.gptlambda.api.service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptlambda.api.props.ScraperProps;
import com.gptlambda.api.service.chat.ChatService;
import com.gptlambda.api.dto.ChatJob;
import com.gptlambda.api.dto.ScrapingJob;
import com.gptlambda.api.dto.SmartProxyJob;
import com.gptlambda.api.service.scraper.ScraperService;
import com.gptlambda.api.service.utils.JobSemaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 6/4/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Consumer {
  private final ObjectMapper objectMapper;
  private final ScraperService scraperService;
  private final ChatService chatService;
  private final JobSemaphore jobSemaphore;
  private final ScraperProps scraperProps;
  private final int numRetries = 3;

  public void scrapeJobConsumer(String message) throws InterruptedException {
    jobSemaphore.getScrapeJobSemaphore().acquire();
    try {
      ScrapingJob job = objectMapper.readValue(message, ScrapingJob.class);
      Thread.startVirtualThread(() -> {
        if (scraperProps.isUseSmartProxy()) {
          scraperService.smartProxyScraper(job);
        } else {
          scraperService.scrape(job.getId(), job.getProductSku(), job.getUrl(), job.getType(), true,
              Math.max(numRetries, job.getRetries()), false,
              job.isInitialReviewsByStarRating(), job.getStarRating());
        }
      });
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private void chatJobConsumer(String message) throws InterruptedException {
    jobSemaphore.getChatJobSemaphore().acquire();
    try {
      ChatJob job = objectMapper.readValue(message, ChatJob.class);
      Thread.startVirtualThread(() ->
          chatService.callGpt(job.getQuery(), job.getFcmToken(), job.getProductSku()));
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private void smartProxyJobConsumer(String message) throws InterruptedException {
    jobSemaphore.getSmartProxyJobSemaphore().acquire();
    try {
      SmartProxyJob job = objectMapper.readValue(message, SmartProxyJob.class);
      Thread.startVirtualThread(() ->
          scraperService.smartProxyResultHandler(job));
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }
}
