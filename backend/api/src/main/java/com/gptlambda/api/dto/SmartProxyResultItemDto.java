package com.gptlambda.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 7/2/23
 */
@Getter
@Setter
public class SmartProxyResultItemDto {
  private String content;
  private String url;

  @JsonProperty("task_id")
  private String taskId;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @JsonProperty("status_code")
  private int statusCode;
}
