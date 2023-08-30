package net.functionhub.api.dto;

import java.util.Map;
import lombok.Getter;

/**
 * @author Biz Melesse created on 8/1/23
 */
@Getter
public class RequestHeaders {

  private final Map<String, String> headers;
  private final Map<String, String> originalHeaders;
  public RequestHeaders(Map<String, String> headers, Map<String, String> originalHeaders) {
    this.headers = headers;
    this.originalHeaders = originalHeaders;
  }
}

