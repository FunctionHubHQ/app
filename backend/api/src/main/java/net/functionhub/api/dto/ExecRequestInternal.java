package net.functionhub.api.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 6/5/23
 */
@Getter @Setter
public class ExecRequestInternal {
  private Map<String, Object> payload;
  private String env;
  private String uid;
  private Long maxCpuTime;
  private Long maxExecutionTime;
  private Long maxMemoryUsage;
  private String execId;
  private Boolean validate;
  private Boolean deployed;
  private String version;
  private String apiKey;
}
