package com.gptlambda.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 7/31/23
 */
@Getter
@Setter
@Slf4j
public class GPTFunctionCall {
  private String name;
  private String arguments;
  private Map<String, Object> requestPayload;

  public void parseRequestPayload(ObjectMapper objectMapper) {
    if (!ObjectUtils.isEmpty(arguments)) {
      TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
      try {
        requestPayload = objectMapper.readValue(arguments, typeRef);
      } catch (JsonProcessingException e) {
        log.error("{}.handleSpecResult: {}",
            getClass().getSimpleName(),
            e.getMessage());
      }
    }
  }
}
