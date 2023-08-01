package com.gptlambda.api.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 6/5/23
 */
@Getter @Setter
public class ExecRequestPayload {
  private Map<String, Object> payload;
  private String env;
  private String uid;
  private String fcmToken;
  private Long timeout;
  private String execId;
  private Boolean validate;
}
