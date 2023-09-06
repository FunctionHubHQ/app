package net.functionhub.proxy.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 9/5/23
 */
@Getter
@Setter
public class HttpRequestHistory {
  private String userId;
  private String httpMethod;
  private String url;
  private String executionId;
  private Long requestStartedAt;
  private Long requestEndedAt;
  private Long requestDuration;
  private Integer httpStatusCode;
  private String errorMessage;
  private Long responseContentLength;
  private Long requestContentLength;
}
