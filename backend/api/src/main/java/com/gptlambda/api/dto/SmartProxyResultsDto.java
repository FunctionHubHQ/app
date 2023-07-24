package com.gptlambda.api.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 7/2/23
 */
@Getter
@Setter
public class SmartProxyResultsDto {
  private List<SmartProxyResultItemDto> results;
}
