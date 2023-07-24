package com.gptlambda.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 7/2/23
 */
@Getter
@Setter
public class SmartProxyJob extends ScrapingJob {
  private String taskId;
  private String resultUrl;
}
