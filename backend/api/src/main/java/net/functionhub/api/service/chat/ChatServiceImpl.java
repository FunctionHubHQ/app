package net.functionhub.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.ExecResultAsync;
import net.functionhub.api.FHCompletionRequest;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.projection.Deployment;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.CommitHistoryRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.dto.GPTFunction;
import net.functionhub.api.dto.GPTFunctionCall;
import net.functionhub.api.dto.GptUsage;
import net.functionhub.api.dto.SessionUser;
import net.functionhub.api.props.MessagesProps;
import net.functionhub.api.props.OpenAiProps;
import net.functionhub.api.dto.RequestHeaders;
import net.functionhub.api.props.SourceProps;
import net.functionhub.api.service.openai.completion.CompletionRequest;
import net.functionhub.api.service.openai.completion.CompletionRequestFunctionalCall;
import net.functionhub.api.service.openai.completion.CompletionResult;
import net.functionhub.api.service.openai.completion.CompletionRequestMessage;
import net.functionhub.api.service.runtime.RuntimeService;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.service.user.UserService.AuthMode;
import net.functionhub.api.service.utils.FHUtils;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.jetty.http.HttpStatus;
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
  private final CodeCellRepo codeCellRepo;
  private final RuntimeService runtimeService;
  private final RequestHeaders requestHeaders;
  private final UserRepo userRepo;
  private final SourceProps sourceProps;
  private final MessagesProps messagesProps;
  private final CommitHistoryRepo commitHistoryRepo;
  private final HttpServletResponse httpServletResponse;
  private final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};

  @Override
  public CompletionRequest buildCompletionRequest(String prompt, List<GPTFunction> functions, String userId,
      String functionCallMode) {
    String content = String.format("PROMPT: %s\n\nANSWER:", prompt);
    log.info("Generated query: {}", content);
    List<CompletionRequestMessage> messages = new ArrayList<>();
    messages.add(new CompletionRequestMessage("system", openAiProps.getSystemMessage()));
    messages.add(new CompletionRequestMessage("user", content));
    return CompletionRequest
        .builder()
        .user(userId)
        .functions(functions)
        .functionCall(functionCallMode)
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
      CodeCellEntity codeCell, boolean deployed) {
    Map<String, Object> response = new HashMap<>();
    CompletionRequest request = buildCompletionRequest(prompt,
        functions, userId, "auto");
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
        GptUsage gptUsage = new GptUsage();
        updateResponse(result, gptUsage, response);
        String content = result.getChoices().get(0).getMessage().getContent();
        GPTFunctionCall functionCall = result.getChoices().get(0).getMessage().getFunctionCall();
        if (ObjectUtils.isEmpty(content) && functionCall != null) {
          String version = functionCall.getVersion();
          String codeId, functionName;
          if (deployed) {
            Deployment deployment = commitHistoryRepo.findDeployedCommitByVersion(version);
            codeId = deployment.getId();
            functionName = deployment.getName();
          } else {
            codeId = codeCell.getUid().toString();
            functionName = codeCell.getFunctionName();
          }

          ExecRequest execRequest = new ExecRequest()
              .uid(codeId)
              .payload(new Gson().toJson(functionCall.getRequestPayload(objectMapper)))
              .execId(UUID.randomUUID().toString())
              .deployed(deployed)
              .version(version) // The version must be specified so that a specific version of a deployment can run
              .validate(false);
          ExecResultAsync execResultAsync = runtimeService.exec(execRequest, deployed);

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
            response.clear();
            response.putAll(objectMapper.readValue(resultStr, typeRef));
            return response;
          }
          updateResponse(result, gptUsage, response);
        }
        return response;
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

  private void updateResponse(CompletionResult result, GptUsage gptUsage,
      Map<String, Object> response) {
    if (result != null && result.getUsage() != null) {
      boolean hasPrevUsage = false;
      long _prevPromptTokens = 0, _prevCompletionTokens = 0, _prevTotalTokens = 0;
      Object prevPromptTokens = gptUsage.getAggregateUsage().get("prompt_tokens");
      Object prevCompletionTokens = gptUsage.getAggregateUsage().get("completion_tokens");
      Object prevTotalTokens = gptUsage.getAggregateUsage().get("total_tokens");
      if (prevPromptTokens != null && prevCompletionTokens != null && prevTotalTokens != null) {
        _prevPromptTokens = (long) prevPromptTokens;
        _prevCompletionTokens = (long) prevCompletionTokens;
        _prevTotalTokens = (long) prevTotalTokens;
        hasPrevUsage = true;
      }
      long newPromptTokens = result.getUsage().getPromptTokens();
      long newCompletionTokens = result.getUsage().getCompletionTokens();
      long newTotalTokens = result.getUsage().getTotalTokens();
      gptUsage.getAggregateUsage().put("prompt_tokens", _prevPromptTokens + newPromptTokens);
      gptUsage.getAggregateUsage().put("completion_tokens", _prevCompletionTokens + newCompletionTokens);
      gptUsage.getAggregateUsage().put("total_tokens", _prevTotalTokens + newTotalTokens);
      if (hasPrevUsage) {
        gptUsage.getAggregateUsage().put("aggregate", true);
        gptUsage.getAggregateUsage().put("description",
            "Aggregate usage includes token count for the initial function call and a subsequent call to GPT");
      }
    }
    if (result != null) {
      response.put("id", result.getId());
      response.put("object", result.getObject());
      response.put("created", result.getCreated());
      response.put("model", result.getModel());
      response.put("usage", gptUsage.getAggregateUsage());
      response.put("choices", result.getChoices());
    }
  }

  @Override
  public Map<String, Object> gptCompletionDevRequest(String functionSlug,
      FHCompletionRequest fhCompletionRequest) {
    Map<String, Object> response = new HashMap<>();
    if (!ObjectUtils.isEmpty(functionSlug) &&
        !ObjectUtils.isEmpty(fhCompletionRequest) &&
        !ObjectUtils.isEmpty(fhCompletionRequest.getPrompt())) {
      CodeCellEntity codeCell = codeCellRepo.findBySlug(functionSlug);
      if (codeCell == null) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Code not found");
        response.put("error", error);
        return response;
      }
       GPTFunction function = buildGptFunctionTestRequest(codeCell);
       response.putAll(gptRequestWithFunctionCall(
           codeCell.getUserId(),
           fhCompletionRequest.getPrompt(),
           List.of(function), codeCell,
           false));
       if (ObjectUtils.isEmpty(response)) {
         Map<String, String> error = new HashMap<>();
         error.put("message", "Unknown error");
         response.put("error", error);
         return response;
       }
       return response;
    }
    Map<String, String> error = new HashMap<>();
    error.put("message", "Operation not supported");
    response.put("error", error);
    return response;
  }

  @Override
  public Map<String, Object> gptCompletionDeployedRequest(FHCompletionRequest completionRequest) {
    String prompt = completionRequest.getPrompt();
    Map<String, Object> response = new HashMap<>();
    if (ObjectUtils.isEmpty(prompt)) {
      Map<String, Object> error = new HashMap<>();
      error.put("message", "You must provide a prompt");
      response.put("error", error);
      return response;
    }
    // TODO: do not load all user functions for this user_id. Apply limits here
    // TODO: Need to implement projects to group functions
    UserProjection user = userRepo.findByApiKey(requestHeaders.getHeaders().get("api-key"));
    //TODO: need to fetch only distinct functions because there could be several versions of the
    // same function previously deployed
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
        true));
    return response;
  }

  @Override
  public Map<String, Object> devGptCompletion(String functionSlug,
      FHCompletionRequest fhCompletionRequest) {
    SessionUser user = FHUtils.getSessionUser();
    if (!user.getAuthMode().name().equals(AuthMode.FB.name()) && !sourceProps.getProfile().equals("test")) {
      FHUtils.raiseHttpError(httpServletResponse,
          objectMapper,
          messagesProps.getUnauthorized(),
          HttpStatus.FORBIDDEN_403);
    }
    return gptCompletionDevRequest(functionSlug, fhCompletionRequest);
  }

  @Override
  public Map<String, Object> prodGptCompletion(FHCompletionRequest fhCompletionRequest) {
    SessionUser user = FHUtils.getSessionUser();
    if (!user.getAuthMode().name().equals(AuthMode.AK.name())) {
      FHUtils.raiseHttpError(httpServletResponse,
          objectMapper,
          messagesProps.getUnauthorized(),
          HttpStatus.FORBIDDEN_403);
    }
    return gptCompletionDeployedRequest(fhCompletionRequest);
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