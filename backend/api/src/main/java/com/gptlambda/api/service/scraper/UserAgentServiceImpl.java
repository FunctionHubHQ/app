package com.gptlambda.api.service.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptlambda.api.dto.UserAgents;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

/**
 * @author Biz Melesse created on 6/8/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAgentServiceImpl implements UserAgentService {
  private final ObjectMapper objectMapper;
  private final List<String> userAgents = new ArrayList<>();

  @PostConstruct
  public void loadUserAgents() {
    String path = "scraping/userAgents.json";
    InputStream resource;
    try {
      resource = new ClassPathResource(path).getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource)) ) {
      String json = FileCopyUtils.copyToString(reader);
      UserAgents agents = objectMapper.readValue(json, UserAgents.class);
      List<String> allAgents = new ArrayList<>();
      allAgents.addAll(agents.getChrome());
      allAgents.addAll(agents.getFirefox());
      allAgents.addAll(agents.getEdge());
      userAgents.addAll(new HashSet<>(allAgents));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getRandomUserAgent() {
    Random rand = new Random();
    return userAgents.get(rand.nextInt(userAgents.size()));
  }
}
