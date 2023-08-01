package com.gptlambda.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Biz Melesse created on 7/31/23
 */
@Getter @Setter
@Slf4j
public class GPTFunction {
  private String name;
  private String description;
  private Map<String, Object> parameters;

  public void setParameters(String payload, ObjectMapper objectMapper) {
    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
    try {
      parameters = objectMapper.readValue(payload, typeRef);
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }
}
