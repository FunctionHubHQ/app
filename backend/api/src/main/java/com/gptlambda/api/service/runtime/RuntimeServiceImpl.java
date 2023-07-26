package com.gptlambda.api.service.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResult;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.dto.SmartProxyTaskDto;
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.chromeExtension.ExtensionServiceImpl.MessageType;
import com.gptlambda.api.service.utils.GPTLambdaUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 7/26/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuntimeServiceImpl implements RuntimeService {
  private final SourceProps sourceProps;
  private final ObjectMapper objectMapper;
  private final String runtimeUrl = "http://localhost:8000/execute";

  @Override
  public GenericResponse exec(ExecRequest execRequest) {
    Map<String, Object> body = new HashMap<>();
    Map<String, Object> payload = new HashMap<>();
    payload.put("day", "Monday");
    payload.put("greeting", "Hello, Spring!");
    body.put("hash", GPTLambdaUtils.generateUid(GPTLambdaUtils.LONG_UID_LENGTH));
    body.put("payload", payload);
    body.put("fcmToken", execRequest.getFcmToken());
    body.put("env", sourceProps.getProfile());
    Thread.startVirtualThread(() -> submitExecutionTask(body, runtimeUrl));
    return new GenericResponse().status("ok");
  }

  @Override
  public String getUserCode(String hash) {
    return "import moment from \"npm:moment\";\n"
        + "\n"
        + "interface RequestPayload {\n"
        + "  greeting?: String,\n"
        + "  day?: String\n"
        + "}\n"
        + "\n"
        + "export default async function(payload: RequestPayload) {\n"
        + "  console.log(\"Default function reached in user code\", xyz)\n"
        + "  return `${payload.day}: ${payload.greeting}, time: ${moment().format('MMMM Do YYYY, h:mm:ss a')}`\n"
        + "}";
  }

  @Override
  public GenericResponse handleExecResult(ExecResult execResult) {
    if (!ObjectUtils.isEmpty(execResult.getFcmToken())) {
      try {
        Message.Builder builder = Message.builder();
        builder.putData("type", MessageType.EXEC_RESULT);
        if (!ObjectUtils.isEmpty(execResult.getResult())) {
          builder.putData("result", objectMapper.writeValueAsString(execResult.getResult()));
        }
        if (!ObjectUtils.isEmpty(execResult.getError())) {
          builder.putData("error", execResult.getError());
        }
        if (!ObjectUtils.isEmpty(execResult.getHash())) {
          builder.putData("hash", execResult.getHash());
        }
        Message message = builder.setToken(execResult.getFcmToken()).build();
        sendFcmMessage(message);
      } catch (JsonProcessingException e) {
        log.error("{}.submitExecutionTask: {}",
            getClass().getSimpleName(),
            e.getMessage());
      }
    }
    return new GenericResponse().status("ok");
  }

  private void sendFcmMessage(Message message) {
    try {
      FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private void submitExecutionTask(Map<String, Object> body, String url) {
      try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entityBody = getEntityBody(body);
        httpPost.setEntity(entityBody);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        httpResponse.close();
      } catch (IOException e) {
        log.error("{}.submitExecutionTask: {}",
            getClass().getSimpleName(),
            e.getMessage());
      }
  }

  private StringEntity getEntityBody(Map<String, Object> body) {
    try {
      return new StringEntity(objectMapper.writeValueAsString(body));
    } catch (JsonProcessingException | UnsupportedEncodingException e) {
      log.error(e.getLocalizedMessage());
    }
    return null;
  }
}
