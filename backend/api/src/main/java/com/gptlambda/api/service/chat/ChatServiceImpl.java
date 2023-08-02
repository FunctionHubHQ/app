package com.gptlambda.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResultAsync;
import com.gptlambda.api.GLCompletionResponse;
import com.gptlambda.api.GLCompletionTestRequest;
import com.gptlambda.api.data.postgres.entity.CodeCellEntity;
import com.gptlambda.api.data.postgres.repo.ChatHistoryRepo;
import com.gptlambda.api.data.postgres.repo.CodeCellRepo;
import com.gptlambda.api.data.postgres.repo.FcmTokenRepo;
import com.gptlambda.api.dto.GPTFunction;
import com.gptlambda.api.dto.GPTFunctionCall;
import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.props.RabbitMQProps;
import com.gptlambda.api.dto.RequestHeaders;
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.openai.completion.CompletionRequest;
import com.gptlambda.api.service.openai.completion.CompletionRequestFunctionalCall;
import com.gptlambda.api.service.openai.completion.CompletionResult;
import com.gptlambda.api.service.openai.completion.CompletionRequestMessage;
import com.gptlambda.api.service.runtime.RuntimeService;
import com.gptlambda.api.service.runtime.RuntimeServiceImpl;
import com.gptlambda.api.service.runtime.RuntimeServiceImpl.MessageType;
import com.gptlambda.api.service.utils.JobSemaphore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 6/12/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
  private final ObjectMapper objectMapper;
  private final OpenAiProps openAiProps;
  private final ChatHistoryRepo chatHistoryRepo;
  private final JobSemaphore jobSemaphore;
  private final FcmTokenRepo fcmTokenRepo;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMQProps rabbitMQProps;
  private final CodeCellRepo codeCellRepo;
  private final SourceProps sourceProps;
  private final RuntimeService runtimeService;
  private final RequestHeaders requestHeaders;

  @Override
  public CompletionRequest buildGptRequest(String prompt, List<GPTFunction> functions, String userId) {
    String content = String.format("PROMPT: %s\n\nANSWER:", prompt);
    log.info("Generated query: {}", content);
    List<CompletionRequestMessage> messages = new ArrayList<>();
//    messages.add(new CompletionMessage("system", openAiProps.getSystemMessage()));
    messages.add(new CompletionRequestMessage("user", content));
    return CompletionRequest
        .builder()
        .user(userId)
        .functions(functions)
        .functionCall("auto")
        .model(openAiProps.getCompletionModel())
        .maxTokens(1000) // TODO: need to be able to bubble up request error messages back to the user
//        .maxTokens(openAiProps.getMaxTokens())
        .temperature(openAiProps.getTemp())
        .messages(messages)
        .build();
  }

  @Override
  public CompletionRequestFunctionalCall buildGptRequestFunctionalCall(String prompt,
      String functionResponse, String functionName, String userId) {
    String content = String.format("PROMPT: %s\n\nANSWER:", prompt);
    log.info("Generated query: {}", content);
    Map<String, Object> userContent = new HashMap<>();
    userContent.put("role", "user");
    userContent.put("content", content);

    Map<String, Object> functionContent = new HashMap<>();
    functionContent.put("role", "function");
    functionContent.put("content", functionResponse);
    functionContent.put("name", functionName);
    List<Map<String, Object>> messages = new ArrayList<>(List.of(
        userContent,
        functionContent
    ));
    return CompletionRequestFunctionalCall
        .builder()
        .user(userId)

        .model(openAiProps.getCompletionModel())
        .maxTokens(1000) // TODO: need to be able to bubble up request error messages back to the user
//        .maxTokens(openAiProps.getMaxTokens())
        .temperature(openAiProps.getTemp())
        .messages(messages)
        .build();
  }

  @Override
  public GLCompletionResponse gptCompletionTestRequest(GLCompletionTestRequest glCompletionRequest) {
    String response = null;
    if (!ObjectUtils.isEmpty(glCompletionRequest.getCodeId()) &&
        !ObjectUtils.isEmpty(glCompletionRequest.getUserId()) &&
        !ObjectUtils.isEmpty(glCompletionRequest.getPrompt())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(glCompletionRequest.getCodeId()));

      if (codeCell != null) {
        // TODO: Get all the functions owned by this user
        // We don't need code cell look up here
        GPTFunction function = new GPTFunction();
        function.setName(codeCell.getFunctionName());
        function.setDescription(codeCell.getDescription());
        function.setParameters(codeCell.getJsonSchema(), objectMapper);

        CompletionRequest request = buildGptRequest(glCompletionRequest.getPrompt(),
            List.of(function), glCompletionRequest.getUserId());
        try {
          String json = objectMapper.writeValueAsString(request);
          log.info("Full request: {}", json);
          CompletionResult result = gptHttpRequest(json);
          if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
              result.getChoices().get(0).getMessage() != null) {
            String content = result.getChoices().get(0).getMessage().getContent();
            GPTFunctionCall functionCall = result.getChoices().get(0).getMessage().getFunctionCall();
            if (ObjectUtils.isEmpty(content) && functionCall != null) {
              functionCall.parseRequestPayload(objectMapper);
              if (ObjectUtils.isEmpty(functionCall.getRequestPayload())) {
                // TODO return the empty response to GPT
              } else {
                Map<String, Object> payload= functionCall.getRequestPayload();
                String version = payload.get(RuntimeServiceImpl.versionKey).toString();
                payload.remove(RuntimeServiceImpl.versionKey);

                // Get the code correct code by version in PROD. Dev/testing should use the provided code cell uid
//                CodeCellEntity deployedCodeCell = codeCellRepo.findByVersion(version);
                ExecRequest execRequest = new ExecRequest()
//                    .uid(deployedCodeCell.getUid().toString())
                    .uid(codeCell.getUid().toString())
                    .payload(functionCall.getRequestPayload())
                    .execId(UUID.randomUUID().toString())
                    .validate(false)
                    .fcmToken(glCompletionRequest.getFcmToken());
                runtimeService.exec(execRequest);
                ExecResultAsync execResultAsync = runtimeService.getExecutionResult(execRequest.getExecId());

                // Call GPT with the function response
                CompletionRequestFunctionalCall requestFunctionalCall = buildGptRequestFunctionalCall(
                    glCompletionRequest.getPrompt(), execResultAsync.getResult(), codeCell.getFunctionName(),
                    codeCell.getUserId());

                json = objectMapper.writeValueAsString(requestFunctionalCall);
                log.info("Full functional request: {}", json);
                result = gptHttpRequest(json);
                if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
                    result.getChoices().get(0).getMessage() != null) {
                  response = result.getChoices().get(0).getMessage().getContent();
                  log.info("Response: {}", response);
                }
              }
            }
//            Thread.startVirtualThread(
//                () -> saveToChatHistory(response, productSku, fcmToken, true));
//            Message message = Message.builder()
//                .putData("content", response)
//                .putData("productSku", productSku)
//                .putData("type", MessageType.CHAT)
//                .setToken(fcmToken)
//                .build();
//            sendFcmMessage(message);
//            log.info("Response - SKU={} queryId={}: {}", productSku, queryId, response);
          }
        } catch (JsonProcessingException e) {
          log.error(e.getLocalizedMessage());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    if (sourceProps.getProfile().equals("prod") || sourceProps.getProfile().equals("dev")) {
      // TODO: send fcm message
      Message message = Message.builder()
                .putData("content", response)
                .putData("uid", glCompletionRequest.getCodeId())
                .putData("type", MessageType.CHAT)
                .setToken(glCompletionRequest.getFcmToken())
                .build();
            sendFcmMessage(message);
    }
    else if (!ObjectUtils.isEmpty(response)) {
      return new GLCompletionResponse()
          .codeCellId(glCompletionRequest.getCodeId())
          .result(response);
    } else {
        return new GLCompletionResponse()
            .codeCellId(glCompletionRequest.getCodeId()).error("Unknown error");
      }
    return new GLCompletionResponse().error("Operation not supported");
  }

  @Override
  public Map<String, Object> gptCompletionDeployedRequest(Map<String, Object> requestBody) {

    return null;
  }


  private void sendFcmMessage(Message message) {
    try {
      FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private CompletionResult gptHttpRequest(String prompt) throws IOException {
    OkHttpClient httpClient = new OkHttpClient.Builder().build();
    RequestBody body = RequestBody.create(
        MediaType.parse("application/json"), prompt);
    Request request = new Request.Builder()
        .url(openAiProps.getCompletionEndpoint())
        .addHeader("Authorization", "Bearer " + openAiProps.getApiKey())
        .post(body)
        .build();
    Call call = httpClient.newCall(request);
    Response response = call.execute();
    String r = response.body().string();
    response.close();
    return objectMapper.readValue(r, CompletionResult.class);
  }
}
