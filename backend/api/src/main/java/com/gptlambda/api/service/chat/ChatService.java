package com.gptlambda.api.service.chat;


import com.gptlambda.api.GLCompletionResponse;
import com.gptlambda.api.GLCompletionTestRequest;
import com.gptlambda.api.dto.GPTFunction;
import com.gptlambda.api.service.openai.completion.CompletionRequest;
import com.gptlambda.api.service.openai.completion.CompletionRequestFunctionalCall;
import java.util.List;
import java.util.Map;

/**
 * @author Biz Melesse created on 6/12/23
 */
public interface ChatService {

  CompletionRequest buildGptRequest(String prompt, List<GPTFunction> functions, String userId);
  CompletionRequestFunctionalCall buildGptRequestFunctionalCall(String prompt,
      String functionResponse, String functionName, String userId);

  /**
   * Run user GPT completion test request. In production, send the response back to the client
   * via FCM. That way, users don't exploit this route for deployed use case. This route has
   * different usage limitations that are more appropriate for testing. Dev use case is for
   * local frontend development.
   *
   * @param glCompletionRequest
   * @return
   */
  GLCompletionResponse gptCompletionTestRequest(GLCompletionTestRequest glCompletionRequest);

  Map<String, Object> gptCompletionDeployedRequest(Map<String, Object> requestBody);

}
