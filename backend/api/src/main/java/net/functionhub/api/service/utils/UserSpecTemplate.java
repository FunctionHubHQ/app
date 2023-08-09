package net.functionhub.api.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.functionhub.api.props.SourceProps;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 8/8/23
 */
@Component
@Getter
@RequiredArgsConstructor
public class UserSpecTemplate {
  private Map<String, Object> spec;
  private final ObjectMapper objectMapper;
  private final SourceProps sourceProps;

  public Map<String, Object> getSpecCopy() {
    return new HashMap<>(spec);
  }

  @PostConstruct
  public void init() {
    String rawSpec = FHUtils.loadFile("openapi/userSpecTemplate.json");
    if (!ObjectUtils.isEmpty(rawSpec)) {
      TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
      try {
        this.spec = objectMapper.readValue(rawSpec, typeRef);
        Map<String, String> servers = new HashMap<>();
        servers.put("url", sourceProps.getBaseUrl());
        this.spec.put("servers", List.of(servers));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
