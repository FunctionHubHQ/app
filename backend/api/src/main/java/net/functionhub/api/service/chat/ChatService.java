package net.functionhub.api.service.chat;


import net.functionhub.api.FHCompletionRequest;
import net.functionhub.api.dto.GPTFunction;
import net.functionhub.api.service.openai.completion.CompletionRequest;
import net.functionhub.api.service.openai.completion.CompletionRequestFunctionalCall;
import java.util.List;
import java.util.Map;

/**
 * @author Biz Melesse created on 6/12/23
 */
public interface ChatService {

  CompletionRequest buildCompletionRequest(String prompt, List<GPTFunction> functions, String userId,
      String functionCallMode);
  CompletionRequestFunctionalCall buildGptRequestFunctionalCall(String prompt,
      String functionResponse, String functionName, String userId);

  /**
   * Run user GPT completion test request. In production, send the response back to the client
   * via FCM. That way, users don't exploit this route for deployed use case. This route has
   * different usage limitations that are more appropriate for testing. Dev use case is for
   * local frontend development.
   *
   * @param functionSlug
   * @param fhCompletionRequest
   * @return
   */
  Map<String, Object> gptCompletionDevRequest(String functionSlug,
      FHCompletionRequest fhCompletionRequest);

  Map<String, Object> gptCompletionDeployedRequest(FHCompletionRequest fhCompletionRequest);


  /**
   * Make a GPT completion request and include the latest user function in the request.
   * In development, function_call is set to true.
   *
   * @param functionSlug
   * @param fhCompletionRequest
   * @return
   */
  Map<String, Object> devGptCompletion(String functionSlug,
      FHCompletionRequest fhCompletionRequest);

  Map<String, Object> prodGptCompletion(FHCompletionRequest fhCompletionRequest);

}
