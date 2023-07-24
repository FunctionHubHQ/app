package com.gptlambda.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 7/2/23
 */
@Getter
@Setter
public class JobQueue {
  private String queue;
  private String routingKey;
  private String routingKeyPrefix;
  private int threadCount;
}
