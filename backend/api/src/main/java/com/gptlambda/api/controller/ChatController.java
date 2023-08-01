package com.gptlambda.api.controller;

import com.gptlambda.api.ChatApi;
import com.gptlambda.api.GLCompletionResponse;
import com.gptlambda.api.GLCompletionTestRequest;
import com.gptlambda.api.service.chat.ChatService;
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
  public ResponseEntity<GLCompletionResponse> gptCompletionTest(
      GLCompletionTestRequest glCompletionTestRequest) {
    return ResponseEntity.ok(chatService.gptCompletionTest(glCompletionTestRequest));
  }
}