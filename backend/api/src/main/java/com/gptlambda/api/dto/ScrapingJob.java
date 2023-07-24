package com.gptlambda.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 6/5/23
 */
@Getter @Setter
public class ScrapingJob {
  public static final int DEFAULT_RETRIES = 5;
  private String url;
  private String productSku;
  private String questionId;
  private boolean initialReviewsByStarRating;
  private String starRating;
  private JobType type;
  private String id;
  private int retries;
  private long productId;
}
