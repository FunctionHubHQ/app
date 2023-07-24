package com.gptlambda.api.service.chat;


import com.gptlambda.api.service.openai.completion.CompletionMessage;
import com.gptlambda.api.service.openai.completion.CompletionRequest;
import java.util.List;

/**
 * @author Biz Melesse created on 6/12/23
 */
public interface ChatService {

  CompletionRequest buildGptRequest(String query, String fcmToken,  String productSku);
  void callGpt(String query, String fcmToken, String productSku);
  void addQueryToQueue(String query, String fcmToken, String productSku);
  String queryBuilder(String query, List<CompletionMessage> conversationHistory, String productSku);

}
