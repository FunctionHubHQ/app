package com.gptlambda.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.gson.Gson;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResultAsync;
import com.gptlambda.api.GLCompletionTestRequest;
import com.gptlambda.api.data.postgres.entity.CodeCellEntity;
import com.gptlambda.api.data.postgres.entity.UserEntity;
import com.gptlambda.api.data.postgres.projection.Deployment;
import com.gptlambda.api.data.postgres.repo.CodeCellRepo;
import com.gptlambda.api.data.postgres.repo.CommitHistoryRepo;
import com.gptlambda.api.data.postgres.repo.UserRepo;
import com.gptlambda.api.dto.GPTFunction;
import com.gptlambda.api.dto.GPTFunctionCall;
import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.dto.RequestHeaders;
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.openai.completion.CompletionRequest;
import com.gptlambda.api.service.openai.completion.CompletionRequestFunctionalCall;
import com.gptlambda.api.service.openai.completion.CompletionResult;
import com.gptlambda.api.service.openai.completion.CompletionRequestMessage;
import com.gptlambda.api.service.runtime.RuntimeService;
import com.gptlambda.api.service.runtime.RuntimeServiceImpl.MessageType;
import java.io.IOException;
import java.net.SocketTimeoutException;
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
//  private final ChatHistoryRepo chatHistoryRepo;
//  private final JobSemaphore jobSemaphore;
//  private final FcmTokenRepo fcmTokenRepo;
//  private final RabbitTemplate rabbitTemplate;
//  private final RabbitMQProps rabbitMQProps;
  private final CodeCellRepo codeCellRepo;
  private final SourceProps sourceProps;
  private final RuntimeService runtimeService;
  private final RequestHeaders requestHeaders;
  private final UserRepo userRepo;
  private final CommitHistoryRepo commitHistoryRepo;
  private final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};;

  @Override
  public CompletionRequest buildCompletionRequest(String prompt, List<GPTFunction> functions, String userId) {
    String content = String.format("PROMPT: %s\n\nANSWER:", prompt);
    log.info("Generated query: {}", content);
    List<CompletionRequestMessage> messages = new ArrayList<>();
    messages.add(new CompletionRequestMessage("system", openAiProps.getSystemMessage()));
    messages.add(new CompletionRequestMessage("user", content));
    return CompletionRequest
        .builder()
        .user(userId)
        .functions(functions)
        .functionCall("auto")
        .model(openAiProps.getCompletionModel())
        .maxTokens(openAiProps.getMaxTokens())
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
        .maxTokens(openAiProps.getMaxTokens())
        .temperature(openAiProps.getTemp())
        .messages(messages)
        .build();
  }

  private Map<String, Object> gptRequestWithFunctionCall(String userId, String prompt, List<GPTFunction> functions,
      CodeCellEntity codeCell, boolean deployed, String fcmToken) {
    Map<String, Object> response = new HashMap<>();
    CompletionRequest request = buildCompletionRequest(prompt,
        functions, userId);
    String json = null;
    try {
      json = objectMapper.writeValueAsString(request);
    log.info("Full request: {}", json);
    CompletionResult result = null;
    try {
      String resultStr = gptHttpRequest(json);
      try {
         result = objectMapper.readValue(resultStr, CompletionResult.class);
         if (ObjectUtils.isEmpty(result.getChoices()) && !ObjectUtils.isEmpty(resultStr)) {
           response.putAll(objectMapper.readValue(resultStr, typeRef));
           return response;
         }
      } catch (JsonProcessingException e) {
        log.error(e.getLocalizedMessage());
      }
      if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
          result.getChoices().get(0).getMessage() != null) {
        String content = result.getChoices().get(0).getMessage().getContent();
        GPTFunctionCall functionCall = result.getChoices().get(0).getMessage().getFunctionCall();
        if (ObjectUtils.isEmpty(content) && functionCall != null) {
          String version = functionCall.getVersion();
          String codeId, functionName;
          if (deployed) {
            Deployment deployment = commitHistoryRepo.findDeployedCommit(version);
            codeId = deployment.getId();
            functionName = deployment.getName();
          } else {
            codeId = codeCell.getUid().toString();
            functionName = codeCell.getFunctionName();
          }

          ExecRequest execRequest = new ExecRequest()
              .uid(codeId)
              .payload(functionCall.getRequestPayload(objectMapper))
              .execId(UUID.randomUUID().toString())
              .deployed(deployed)
              .version(version) // The version must be specified so that a specific version of deployment can run
              .validate(false)
              .fcmToken(fcmToken);
          runtimeService.exec(execRequest);
          ExecResultAsync execResultAsync = runtimeService.getExecutionResult(
              execRequest.getExecId());

          // Call GPT with the function response
          CompletionRequestFunctionalCall requestFunctionalCall = buildGptRequestFunctionalCall(
              prompt, execResultAsync.getResult(),
              functionName,
              userId);

          json = objectMapper.writeValueAsString(requestFunctionalCall);
          log.info("Full functional request: {}", json);
          resultStr = gptHttpRequest(json);
          result = objectMapper.readValue(resultStr, CompletionResult.class);
          if (ObjectUtils.isEmpty(result.getChoices()) && !ObjectUtils.isEmpty(resultStr)) {
            response.putAll(objectMapper.readValue(resultStr, typeRef));
            return response;
          }

          if (!ObjectUtils.isEmpty(result.getChoices()) &&
              result.getChoices().get(0).getMessage() != null) {
            content = result.getChoices().get(0).getMessage().getContent();
            log.info("Response: {}", content);
            response.put("content", content);
            return response;
          }
        }
        else {
          response.put("content", content);
          return response;
        }
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      Map<String, String> error = new HashMap<>();
      error.put("message", e.getLocalizedMessage());
      response.put("error", error);
      return response;
    }
    } catch (IOException e) {
      e.printStackTrace();
      Map<String, String> error = new HashMap<>();
      error.put("message", e.getLocalizedMessage());
      response.put("error", error);
      return response;
    }
    Map<String, String> error = new HashMap<>();
    error.put("message", "Unknown Error");
    response.put("error", error);
    return response;
  }

  @Override
  public Map<String, Object> gptCompletionTestRequest(GLCompletionTestRequest glCompletionRequest) {
    Map<String, Object> response = new HashMap<>();
    if (!ObjectUtils.isEmpty(glCompletionRequest.getCodeId()) &&
        !ObjectUtils.isEmpty(glCompletionRequest.getUserId()) &&
        !ObjectUtils.isEmpty(glCompletionRequest.getPrompt())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(glCompletionRequest.getCodeId()));
      if (codeCell == null) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Code not found");
        response.put("error", error);
        return response;
      }
       GPTFunction function = buildGptFunctionTestRequest(codeCell);
       response.putAll(gptRequestWithFunctionCall(
        codeCell.getUserId(),
        glCompletionRequest.getPrompt(),
        List.of(function), codeCell,
        false,
        glCompletionRequest.getFcmToken()));

      if (sourceProps.getProfile().equals("prod") || sourceProps.getProfile().equals("dev")) {
        Message message = Message.builder()
            .putData("error", !ObjectUtils.isEmpty(response.get("error")) ?
                new Gson().toJson(response.get("error")) : "")
            .putData("content", !ObjectUtils.isEmpty(response.get("result")) ?
                response.get("result").toString() : "")
            .putData("uid", glCompletionRequest.getCodeId())
            .putData("type", MessageType.CHAT)
            .setToken(glCompletionRequest.getFcmToken())
            .build();
        sendFcmMessage(message);
      }
      else if (!ObjectUtils.isEmpty(response)) {
        response.put("code_cell_id", (glCompletionRequest.getCodeId()));
        response.put("fcm_token", (glCompletionRequest.getFcmToken()));
        return response;
      } else {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Unknown error");
        response.put("error", error);
        return response;
      }
    }
    Map<String, String> error = new HashMap<>();
    error.put("message", "Operation not supported");
    response.put("error", error);
    return response;
  }

  private GPTFunction buildGptFunctionTestRequest(CodeCellEntity codeCell) {
    GPTFunction function = new GPTFunction();
    function.setName(codeCell.getFunctionName());
    function.setDescription(codeCell.getDescription());
    function.setParameters(codeCell.getJsonSchema(), objectMapper);
    return function;
  }

  private GPTFunction buildGptFunctionDeployedRequest(Deployment deployment) {
    GPTFunction function = new GPTFunction();
    // an appended version ensures that we can always uniquely identify the
    // function even if there are duplicates in the same namespace. This hack
    // is guaranteed to as opposed to injecting the version number into the
    // request payload as a required field.
    function.setName(deployment.getName() + "_" + deployment.getVersion());
    function.setDescription(deployment.getDescription());
    function.setParameters(deployment.getPayload(), objectMapper);
    return function;
  }

  @Override
  public Map<String, Object> gptCompletionDeployedRequest(Map<String, Object> requestBody) {
    String prompt = requestBody.get("prompt").toString();
    Map<String, Object> response = new HashMap<>();
    if (ObjectUtils.isEmpty(prompt)) {
      Map<String, String> error = new HashMap<>();
      error.put("message", "You must provide a prompt");
      response.put("error", error);
      return response;
    }
    // TODO: do not load all user functions for this user_id. Apply limits here
    // TODO: Need to implement projects to group functions
    UserEntity user = userRepo.findByApiKey(requestHeaders.getHeaders().get("api-key"));
    List<Deployment> deployments = commitHistoryRepo.findAllDeployedCommits(user.getUid());
    List<GPTFunction> functions = deployments
        .stream()
        .map(this::buildGptFunctionDeployedRequest)
        .toList();

    response.putAll(gptRequestWithFunctionCall(
        user.getUid(),
        prompt,
        functions,
        null,
        true,
        null));
    return response;
  }


  private void sendFcmMessage(Message message) {
    try {
      FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private String gptHttpRequest(String prompt) throws IOException {
    OkHttpClient httpClient = new OkHttpClient.Builder().build();
    RequestBody body = RequestBody.create(
        MediaType.parse("application/json"), prompt);
    Request request = new Request.Builder()
        .url(openAiProps.getCompletionEndpoint())
        .addHeader("Authorization", "Bearer " + openAiProps.getApiKey())
        .post(body)
        .build();
    Call call = httpClient.newCall(request);
    try {
      Response response = call.execute();
      String r = response.body().string();
      response.close();
      return r;
    } catch (SocketTimeoutException e) {
      Map<String, Object> err = new HashMap<>();
      err.put("message", e.getLocalizedMessage());
      return new Gson().toJson(err);
    }
  }
}
