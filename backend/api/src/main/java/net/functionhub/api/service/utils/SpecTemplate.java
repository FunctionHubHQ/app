package net.functionhub.api.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class SpecTemplate {
  private Map<String, Object> userSpec;
  private Map<String, Object> gptProdSpec;
  private Map<String, Object> gptDevSpec;
  private final ObjectMapper objectMapper;
  private final SourceProps sourceProps;

  public Map<String, Object> getUserSpec() {
    return new HashMap<>(userSpec);
  }

  public Map<String, Object> getGptProdSpec() {
    return new HashMap<>(gptProdSpec);
  }

  public Map<String, Object> getGptDevSpec() {
    return new HashMap<>(gptDevSpec);
  }

  // TODO: Create a new file for GPT prod spec
  @PostConstruct
  public void init() {
    this.userSpec = loadSpec("userSpecTemplate");
    this.gptProdSpec = loadSpec("gptProdSpecTemplate");
    this.gptDevSpec = loadSpec("gptDevSpecTemplate");

  }

  private Map<String, Object> loadSpec(String name) {
    String rawSpec = FHUtils.loadFile(String.format("spec/%s.json", name));
    Map<String, Object> spec = new HashMap<>();
    if (!ObjectUtils.isEmpty(rawSpec)) {
      TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
      try {
        spec = objectMapper.readValue(rawSpec, typeRef);
        Map<String, String> servers = new HashMap<>();
        servers.put("url", sourceProps.getBaseUrl());
        spec.put("servers", List.of(servers));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return spec;
  }
}
