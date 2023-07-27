package com.gptlambda.api.service.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.gptlambda.api.Code;
import com.gptlambda.api.CodeUpdateResponse;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResult;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.data.postgres.entity.CodeCellEntity;
import com.gptlambda.api.data.postgres.repo.CodeCellRepo;
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.utils.GPTLambdaUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
  private final CodeCellRepo codeCellRepo;
  private final Slugify slugify;
  private final String runtimeUrl = "http://localhost:8000/execute"; // in docker: http://runtime:8000/execute";

  public static final class MessageType {
    public static final String EXEC_RESULT = "EXEC_RESULT";
    public static final String CHAT = "CHAT";
    public static final String HEART_BEAT = "HEART_BEAT";
    public static final String PRODUCT_INIT = "PRODUCT_INIT";
    public static final String FCM_TOKEN = "FCM_TOKEN";
    public static final String PRODUCT_INFO_REQUEST = "PRODUCT_INFO_REQUEST";
  }

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
  public String getUserCode(String uid) {
    CodeCellEntity entity = codeCellRepo.findByUid(UUID.fromString(uid));
    if (entity != null) {
      return entity.getCode();
    }
    return null;
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
        if (!ObjectUtils.isEmpty(execResult.getUid())) {
          builder.putData("hash", execResult.getUid());
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

  @Override
  public String generateCodeVersion() {
    return GPTLambdaUtils.generateUid(GPTLambdaUtils.LONG_UID_LENGTH);
  }

  @Override
  public CodeUpdateResponse updateCode(Code code) {
    String userId = code.getUserId();
    String codeId = null;
    if (!ObjectUtils.isEmpty(userId)) {
      if (ObjectUtils.isEmpty(code.getUid())) {
        CodeCellEntity codeCell = new CodeCellEntity();
        codeCell.setUid(UUID.randomUUID());
        codeCell.setCode(code.getCode());
        codeCell.setDescription(code.getDescription());
        codeCell.setUserId(userId);
        codeCell.setIsActive(isBelowActiveLimit(code.getUserId()));
        codeCell.setIsPublic(false);
        codeCell.setFunctionName(code.getFunctionName());
        codeCell.setSlug(slugify.toSlug(code.getFunctionName()));
        codeCell.setVersion(generateCodeVersion());
        codeCell.setRequestDto(code.getRequestDto());
        codeCell.setResponseDto(code.getResponseDto());
        codeCellRepo.save(codeCell);
        codeId = codeCell.getUid().toString();
      }
    } else {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(code.getUid()));
      if (codeCell != null && !ObjectUtils.isEmpty(code.getFieldsToUpdate())) {
        for (String field : code.getFieldsToUpdate()) {
          if (field.equals("code")) {
            codeCell.setCode(code.getCode());
            codeCell.setVersion(generateCodeVersion());
          } else if (field.equals("description")) {
            codeCell.setDescription(code.getDescription());
          } else if (field.equals("is_active")) {
            if (isBelowActiveLimit(userId) || !code.getIsActive()) {
              codeCell.setIsActive(code.getIsActive());
            }
          } else if (field.equals("is_public")) {
            codeCell.setIsPublic(code.getIsPublic());
          } else if (field.equals("function_name")) {
            codeCell.setFunctionName(code.getFunctionName());
            codeCell.setSlug(slugify.toSlug(code.getFunctionName()));
          } else if (field.equals("request_dto")) {
            codeCell.setRequestDto(code.getRequestDto());
          } else if (field.equals("response_dto")) {
            codeCell.setResponseDto(code.getRequestDto());
          }
        }
        codeCellRepo.save(codeCell);
        codeId = codeCell.getUid().toString();
      }
    }
    return new CodeUpdateResponse().uid(codeId);
  }

  @Override
  public Code getCodeDetail(String uid) {
    if (!ObjectUtils.isEmpty(uid)) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(uid));
      if (codeCell != null) {
        return new Code()
            .code(codeCell.getCode())
            .uid(uid)
            .userId(codeCell.getUserId())
            .isActive(codeCell.getIsActive())
            .isPublic(codeCell.getIsPublic())
            .functionName(codeCell.getFunctionName())
            .requestDto(codeCell.getRequestDto())
            .responseDto(codeCell.getResponseDto())
            .description(codeCell.getDescription());
      }
    }
    return null;
  }

  private String oToString(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      // pass
    }
    return "{}";
  }

  private Boolean isBelowActiveLimit(String userId) {
    return codeCellRepo.numActiveCells(userId) < Integer.MAX_VALUE;
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
