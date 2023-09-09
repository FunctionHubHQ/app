package net.functionhub.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 6/5/23
 */
@Getter @Setter
public class GenerateSpecRequest {
  private String file;
  private String env;
  private String codeId;
  private String from;
  private String to;
  private String apiKey;
}
