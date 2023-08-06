package net.functionhub.api.controller;

import net.functionhub.api.ChatApi;
import net.functionhub.api.GLCompletionTestRequest;
import net.functionhub.api.service.chat.ChatService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse
 * created on 7/24/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController implements ChatApi {
  private final ChatService chatService;

  @Override
  public ResponseEntity<Map<String, Object>> gptCompletionDeployedRequest(
      Map<String, Object> requestBody) {
    return ResponseEntity.ok(chatService.gptCompletionDeployedRequest(requestBody));
  }

  @Override
  public ResponseEntity<Map<String, Object>> gptCompletionTestRequest(
      GLCompletionTestRequest glCompletionTestRequest) {
    return ResponseEntity.ok(chatService.gptCompletionTestRequest(glCompletionTestRequest));
  }
}