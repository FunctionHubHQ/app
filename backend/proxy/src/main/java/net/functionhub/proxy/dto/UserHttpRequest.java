package net.functionhub.proxy.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 8/30/23
 */
@Getter
@Setter
public class UserHttpRequest {
  private Map<String, String> headers = new HashMap<>();
  private String proxyTarget;
  private String fhApiKey;
}
