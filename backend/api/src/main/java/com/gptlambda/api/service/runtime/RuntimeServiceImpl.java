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
import com.gptlambda.api.SpecResult;
import com.gptlambda.api.data.postgres.entity.CodeCellEntity;
import com.gptlambda.api.data.postgres.entity.CommitHistoryEntity;
import com.gptlambda.api.data.postgres.repo.CodeCellRepo;
import com.gptlambda.api.data.postgres.repo.CommitHistoryRepo;
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.utils.GPTLambdaUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.Base64;
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
  private final CommitHistoryRepo commitHistoryRepo;
  private final Slugify slugify;
  private final String runtimeUrl = "http://localhost:8000/execute"; // in docker: http://runtime:8000/execute";
  private final String tsToOaUrl = "http://localhost:9000/code-gen/ts-to-oa";
  private final String oaToTsUrl = "http://localhost:9000/code-gen/oa-to-ts";

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
    body.put("timeout", 5000);
    Thread.startVirtualThread(() -> submitExecutionTask(body, runtimeUrl));
    return new GenericResponse().status("ok");
  }

  @Override
  public String getUserCode(String uid) {
    CodeCellEntity entity = codeCellRepo.findByUid(UUID.fromString(uid));
    if (entity != null && !ObjectUtils.isEmpty(entity.getCode())) {
      return new String(new Base64().decode(entity.getCode().getBytes()));
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
    CodeCellEntity updatedCell = null;
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
        if (!ObjectUtils.isEmpty(code.getParentId())) {
          codeCell.setParentId(UUID.fromString(code.getParentId()));
        }
        codeCellRepo.save(codeCell);
        updatedCell = codeCell;
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
        codeCell.setUpdatedAt(LocalDateTime.now());
        codeCellRepo.save(codeCell);
        updatedCell = codeCell;
      }
    }
    if (updatedCell != null) {
      CommitHistoryEntity commitHistory = new CommitHistoryEntity();
      commitHistory.setUid(UUID.randomUUID());
      commitHistory.setCodeCellId(updatedCell.getUid());
      commitHistory.setVersion(updatedCell.getVersion());
      commitHistory.setMessage(null);
      commitHistory.setCode(updatedCell.getCode());
      commitHistoryRepo.save(commitHistory);
      return new CodeUpdateResponse().uid(updatedCell.getUid().toString());
    }
    return new CodeUpdateResponse();
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
            .requestDto(codeCell.getRequestDto())
            .responseDto(codeCell.getResponseDto())
            .description(codeCell.getDescription())
            .functionName(codeCell.getFunctionName())
            .updatedAt(codeCell.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
            .createdAt(codeCell.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
      }
    }
    return null;
  }

  @Override
  public String deleteMe(String uid) {
    generateOpenApiSpec(uid);
    return "Submitted!";
  }

  @Override
  public String generateOpenApiSpec(String tsInterface) {
    String file = "export interface Author {\n"
        + "  name: string;\n"
        + "  image: string;\n"
        + "  designation: string;\n"
        + "};\n"
        + "\n"
        + "export interface Blog {\n"
        + "  id: number;\n"
        + "  title: string;\n"
        + "  paragraph: string;\n"
        + "  image: string;\n"
        + "  author: Author;\n"
        + "  tags: string[];\n"
        + "  publishDate: string;\n"
        + "};";

    String ts = "export interface Author {\n"
        + "  name: string;\n"
        + "  image: string;\n"
        + "  designation: string;\n"
        + "};\n"
        + "\n"
        + "export interface ResponsePayload {\n"
        + "  id: number;\n"
        + "  title: string;\n"
        + "  paragraph: string;\n"
        + "  image: string;\n"
        + "  author: Author;\n"
        + "  tags: string[];\n"
        + "  publishDate: string;\n"
        + "};\n"
        + "\n"
        + "export interface RequestPayload {\n"
        + "  id: number;\n"
        + "  title: string;\n"
        + "  paragraph: string;\n"
        + "  image: string;\n"
        + "  author: Author;\n"
        + "  tags: string[];\n"
        + "  publishDate: string;\n"
        + "};\n"
        + "\n"
        + "export default async function(payload: RequestPayload) {\n"
        + "  console.log(\"Hello, world!\")\n"
        + "}";
    String json = "{\n"
        + "  \"openapi\": \"3.0.0\",\n"
        + "  \"info\": {\n"
        + "    \"title\": \"GPT Lambda API\",\n"
        + "    \"version\": \"v1\",\n"
        + "    \"x-comment\": \"Generated by core-types-json-schema (https://github.com/grantila/core-types-json-schema) on behalf of typeconv (https://github.com/grantila/typeconv)\"\n"
        + "  },\n"
        + "  \"paths\": {},\n"
        + "  \"components\": {\n"
        + "    \"schemas\": {\n"
        + "      \"Author\": {\n"
        + "        \"properties\": {\n"
        + "          \"name\": {\n"
        + "            \"description\": \"Author.name\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"image\": {\n"
        + "            \"description\": \"Author.image\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"designation\": {\n"
        + "            \"description\": \"Author.designation\",\n"
        + "            \"type\": \"string\"\n"
        + "          }\n"
        + "        },\n"
        + "        \"required\": [\n"
        + "          \"name\",\n"
        + "          \"image\",\n"
        + "          \"designation\"\n"
        + "        ],\n"
        + "        \"additionalProperties\": false,\n"
        + "        \"description\": \"Author\",\n"
        + "        \"type\": \"object\"\n"
        + "      },\n"
        + "      \"ResponsePayload\": {\n"
        + "        \"properties\": {\n"
        + "          \"id\": {\n"
        + "            \"description\": \"ResponsePayload.id\",\n"
        + "            \"type\": \"number\"\n"
        + "          },\n"
        + "          \"title\": {\n"
        + "            \"description\": \"ResponsePayload.title\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"paragraph\": {\n"
        + "            \"description\": \"ResponsePayload.paragraph\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"image\": {\n"
        + "            \"description\": \"ResponsePayload.image\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"author\": {\n"
        + "            \"$ref\": \"#/components/schemas/Author\",\n"
        + "            \"description\": \"ResponsePayload.author\"\n"
        + "          },\n"
        + "          \"tags\": {\n"
        + "            \"items\": {\n"
        + "              \"description\": \"ResponsePayload.tags.[]\",\n"
        + "              \"type\": \"string\"\n"
        + "            },\n"
        + "            \"description\": \"ResponsePayload.tags\",\n"
        + "            \"type\": \"array\"\n"
        + "          },\n"
        + "          \"publishDate\": {\n"
        + "            \"description\": \"ResponsePayload.publishDate\",\n"
        + "            \"type\": \"string\"\n"
        + "          }\n"
        + "        },\n"
        + "        \"required\": [\n"
        + "          \"id\",\n"
        + "          \"title\",\n"
        + "          \"paragraph\",\n"
        + "          \"image\",\n"
        + "          \"author\",\n"
        + "          \"tags\",\n"
        + "          \"publishDate\"\n"
        + "        ],\n"
        + "        \"additionalProperties\": false,\n"
        + "        \"description\": \"ResponsePayload\",\n"
        + "        \"type\": \"object\"\n"
        + "      },\n"
        + "      \"RequestPayload\": {\n"
        + "        \"properties\": {\n"
        + "          \"id\": {\n"
        + "            \"description\": \"RequestPayload.id\",\n"
        + "            \"type\": \"number\"\n"
        + "          },\n"
        + "          \"title\": {\n"
        + "            \"description\": \"RequestPayload.title\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"paragraph\": {\n"
        + "            \"description\": \"RequestPayload.paragraph\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"image\": {\n"
        + "            \"description\": \"RequestPayload.image\",\n"
        + "            \"type\": \"string\"\n"
        + "          },\n"
        + "          \"author\": {\n"
        + "            \"$ref\": \"#/components/schemas/Author\",\n"
        + "            \"description\": \"RequestPayload.author\"\n"
        + "          },\n"
        + "          \"tags\": {\n"
        + "            \"items\": {\n"
        + "              \"description\": \"RequestPayload.tags.[]\",\n"
        + "              \"type\": \"string\"\n"
        + "            },\n"
        + "            \"description\": \"RequestPayload.tags\",\n"
        + "            \"type\": \"array\"\n"
        + "          },\n"
        + "          \"publishDate\": {\n"
        + "            \"description\": \"RequestPayload.publishDate\",\n"
        + "            \"type\": \"string\"\n"
        + "          }\n"
        + "        },\n"
        + "        \"required\": [\n"
        + "          \"id\",\n"
        + "          \"title\",\n"
        + "          \"paragraph\",\n"
        + "          \"image\",\n"
        + "          \"author\",\n"
        + "          \"tags\",\n"
        + "          \"publishDate\"\n"
        + "        ],\n"
        + "        \"additionalProperties\": false,\n"
        + "        \"description\": \"RequestPayload\",\n"
        + "        \"type\": \"object\"\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + "}";
    Map<String, Object> payload = new HashMap<>();
    payload.put("file", json);
    payload.put("env", sourceProps.getProfile());
    payload.put("uid", "hello-world");
    submitExecutionTask(payload, oaToTsUrl);

//    Map<String, Object> dto = new HashMap<>();
//    try {
//      JsonNode node = objectMapper.readValue(payload, JsonNode.class);
////      walk(node, dto);
//      return objectMapper.writeValueAsString(dto);
//    } catch (JsonProcessingException e) {
//      log.error("{}.generatePayloadDto: {}",
//          getClass().getSimpleName(),
//          e.getMessage());
//    }
    return null;
  }

  @Override
  public GenericResponse handleSpecResult(SpecResult specResult) {
      String spec = specResult.getSpec().getValue();
      if (specResult.getSpec().getFormat().equals("ts")) {
        spec = spec.replace("/**", "\n/**");
      }
      log.info("Spec: {}", spec);
    return new GenericResponse().status("ok");
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
