package com.gptlambda.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
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
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.openai.completion.CompletionRequest;
import com.gptlambda.api.service.openai.completion.CompletionResult;
import com.gptlambda.api.service.openai.completion.CompletionRequestMessage;
import com.gptlambda.api.service.runtime.RuntimeService;
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

//  List<CompletionMessage> getChatHistory(String fcmToken, String productSku) {
//    List<CompletionMessage> history = new ArrayList<>();
//    // TODO: keep track of the total length of the strings to not exceed context window limit
//    FcmTokenEntity tokenEntity = fcmTokenRepo.findByFcmToken(fcmToken);
//    if (tokenEntity != null) {
//      List<ChatHistoryEntity> historyEntities = chatHistoryRepo.findLastNMessages(
//          tokenEntity.getId(),
//          productSku,
//          otherProps.getLastNHistoryMessages());
//      for (ChatHistoryEntity historyEntity : historyEntities) {
//        history.add(new CompletionMessage(historyEntity.getIsGpt() ? "assistant" : "user",
//            historyEntity.getMessage()));
//      }
//    }
//    // Reverse the list so the messages are sorted in ascending order time-wise
//    Collections.reverse(history);
//    return history;
//  }

//  @Override
//  public void callGpt(String query, String fcmToken, String productSku) {
//    String queryId = GPTLambdaUtils.generateUid(GPTLambdaUtils.LONG_UID_LENGTH);
//    log.info("Query - SKU={} queryId={}: {}", productSku, queryId, query);
//    if (query.toLowerCase().contains("feedback")) {
//      handleFeedback(query, fcmToken, productSku);
//    } else {
//      CompletionRequest request = buildGptRequest(query, fcmToken, productSku);
//      Thread.startVirtualThread(
//          () -> saveToChatHistory(query, productSku, fcmToken, false));
//      if (request == null) {
//        Message message = Message.builder()
//            .putData("content", "Sorry, I'm unable to find answers for this product.")
//            .putData("productSku", productSku)
//            .putData("type", MessageType.CHAT)
//            .setToken(fcmToken)
//            .build();
//        sendFcmMessage(message);
//      } else {
//        try {
//          String json = objectMapper.writeValueAsString(request);
//          log.info("Full request: {}", json);
//          CompletionResult result = gptHttpRequest(json);
//          if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
//              result.getChoices().get(0).getMessage() != null) {
//            String response = result.getChoices().get(0).getMessage().getContent();
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
//          }
//        } catch (JsonProcessingException e) {
//          log.error(e.getLocalizedMessage());
//        }
//      }
//    }
//    jobSemaphore.getChatJobSemaphore().release();
//  }

//  private void handleFeedback(String query, String fcmToken, String productSku) {
//    FeedbackEntity entity = new FeedbackEntity();
//    entity.setBody(query);
//    entity.setSubject(fcmToken);
//    entity.setUserId(-1L);
//    feedbackRepo.save(entity);
//    Message message = Message.builder()
//        .putData("content",
//            String.format("We really appreciate your feedback! Please don't forget to give us 5 "
//            + "stars <a href=%s target=\"_blank\"><strong style=\"color: red;\">here</strong></a>. This will help us "
//                    + "continue to make Shoppiem better.",
//                    otherProps.getChromeExtensionStoreUrl()))
//        .putData("productSku", productSku)
//        .putData("type", MessageType.CHAT)
//        .setToken(fcmToken)
//        .build();
//    sendFcmMessage(message);
//  }

//  @Override
//  public void addQueryToQueue(String query, String fcmToken, String productSku) {
//    Thread.startVirtualThread(() -> {
//      ChatJob job = new ChatJob();
//      job.setId(GPTLambdaUtils.generateUid(GPTLambdaUtils.SHORT_UID_LENGTH));
//      job.setQuery(query);
//      job.setFcmToken(fcmToken);
//      job.setProductSku(productSku);
//      // If the product is ready, add the job to the queue immediately. Otherwise, delay the queueing
//      try {
//        String jobString = objectMapper.writeValueAsString(job);
//        rabbitTemplate.convertAndSend(
//            rabbitMQProps.getTopicExchange(),
//            rabbitMQProps
//                .getJobQueues()
//                .get(RabbitMQProps.CHAT_JOB_QUEUE_KEY)
//                .getRoutingKeyPrefix() + productSku,
//            jobString);
//      } catch (JsonProcessingException e) {
//        log.error(e.getLocalizedMessage());
//      }
//    });
//  }

//  @Override
//  public String queryBuilder(String query, List<CompletionMessage> conversationHistory, String productSku) {
//    Collections.reverse(conversationHistory);
//    String history = conversationHistory
//        .stream()
//        .map(it -> String.format("%s: %s", it.getRole().toUpperCase(), it.getContent()))
//        .collect(Collectors.joining("\n"));
//    String systemMessage = openAiProps.getQueryBuilderPrompt();
//    String userMessage = String.format("USER PROMPT: %s\n\nCONVERSATION LOG: \n%s\n\nGENERATED QUESTION:;",
//        query, history);
//    CompletionRequest request =  CompletionRequest
//        .builder()
//        .user(productSku)
//        .model(openAiProps.getCompletionModel())
//        .maxTokens(openAiProps.getMaxTokens())
//        .temperature(openAiProps.getTemp())
//        .messages(List.of(
//            new CompletionMessage("system", systemMessage),
//            new CompletionMessage("user", userMessage)))
//        .build();
//    try {
//      CompletionResult result = gptHttpRequest(objectMapper.writeValueAsString(request));
//      if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
//          result.getChoices().get(0).getMessage() != null) {
//        return result.getChoices().get(0).getMessage().getContent();
//      }
//    } catch (Exception e) {
//      log.error(e.getLocalizedMessage());
//    }
//    return query;
//  }


  @Override
  public GLCompletionResponse gptCompletionTest(GLCompletionTestRequest glCompletionRequest) {
    String response = null;
    if (!ObjectUtils.isEmpty(glCompletionRequest.getCodeId()) &&
        !ObjectUtils.isEmpty(glCompletionRequest.getUserId()) &&
        !ObjectUtils.isEmpty(glCompletionRequest.getPrompt())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(glCompletionRequest.getCodeId()));

      if (codeCell != null) {
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

              int x = 1;
//              runtimeService.exec()
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
//      Message message = Message.builder()
//                .putData("content", response)
//                .putData("productSku", productSku)
//                .putData("type", MessageType.CHAT)
//                .setToken(fcmToken)
//                .build();
//            sendFcmMessage(message);
    } else {
      if (!ObjectUtils.isEmpty(response)) {
        return new GLCompletionResponse()
            .codeCellId(glCompletionRequest.getCodeId()).result(response);
      } else {
        return new GLCompletionResponse()
            .codeCellId(glCompletionRequest.getCodeId()).error("Error message here");
      }

    }
    return new GLCompletionResponse().error("Operation not supported");
  }

//  private void saveToChatHistory(String query, String productSku, String fcmToken, boolean isGpt) {
//    ChatHistoryEntity entity = new ChatHistoryEntity();
//    entity.setMessage(query);
//    entity.setIsGpt(isGpt);
//    entity.setProductSku(productSku);
//    entity.setChatId(GPTLambdaUtils.generateUid(GPTLambdaUtils.LONG_UID_LENGTH));
//
//    FcmTokenEntity fcmTokenEntity = fcmTokenRepo.findByFcmToken(fcmToken);
//    if (fcmTokenEntity == null) {
//      fcmTokenEntity = new FcmTokenEntity();
//      fcmTokenEntity.setFcmToken(fcmToken);
//      fcmTokenRepo.save(fcmTokenEntity);
//    }
//    entity.setFcmTokenId(fcmTokenEntity.getId());
//    chatHistoryRepo.save(entity);
//  }

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
