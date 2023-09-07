package net.functionhub.api.controller;

import java.io.IOException;
import java.util.Map;
import net.functionhub.api.ChatApi;
import net.functionhub.api.GPTCompletionRequest;
import net.functionhub.api.service.chat.ChatService;
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
  public ResponseEntity<Map<String, Object>> devGptCompletion(String functionSlug,
      GPTCompletionRequest fhCompletionRequest) {
    try {
      return ResponseEntity.ok(chatService.devGptCompletion(functionSlug, fhCompletionRequest));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> prodCompletionRequest(
      GPTCompletionRequest fhCompletionRequest) {
    try {
      return ResponseEntity.ok(chatService.prodGptCompletion(fhCompletionRequest));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}