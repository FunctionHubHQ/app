package net.functionhub.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 9/6/23
 */
@Getter
@Setter
public class GptUsage {
  @JsonProperty("aggregate_usage")
  Map<String, Object> aggregateUsage = new HashMap<>();
}
