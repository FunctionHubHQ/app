package com.gptlambda.api.dto;

import java.util.List;
import lombok.Data;

/**
 * @author Biz Melesse created on 6/8/23
 */
@Data
public class UserAgents {
  private List<String> chrome;
  private List<String> firefox;
  private List<String> edge;
}
