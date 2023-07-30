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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
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
import org.springframework.util.ResourceUtils;

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
    if (!ObjectUtils.isEmpty(execRequest.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(execRequest.getUid()));
      if (codeCell != null) {
        String version = codeCell.getVersion();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        payload.put("day", "Monday");
        payload.put("greeting", "Hello, Spring!");
        body.put("uid", execRequest.getUid() + "@" +version);
        body.put("payload", payload);
        body.put("fcmToken", execRequest.getFcmToken());
        body.put("env", sourceProps.getProfile());
        body.put("timeout", 5000);
        Thread.startVirtualThread(() -> submitExecutionTask(body, runtimeUrl));
      }
    }
    return new GenericResponse().status("ok");
  }

  @Override
  public String getUserCode(String uid) {
    if (uid != null) {
      uid = parseUid(uid);
      CodeCellEntity entity = codeCellRepo.findByUid(UUID.fromString(uid));
      if (entity != null && !ObjectUtils.isEmpty(entity.getCode())) {
        String rawCode = new String(Base64.getDecoder().decode(entity.getCode().getBytes()));
        String interfaces = new String(Base64.getDecoder().decode(entity.getInterfaces().getBytes()));
        return workerScript(rawCode, interfaces);
      }
    }

    return null;
////    return null;
//    return "\n"
//        + "import moment from \"npm:moment\";\n"
//        + "\n"
//        + "export async function handler(params: RequestPayload) {\n"
//        + "  console.log(`${moment().format('MMMM Do YYYY, h:mm:ss a')} DEBUG: Event received...thank you!`);\n"
//        + "  console.log(\"// 1. Test ability to send the result back\\n\" +\n"
//        + "      \"// 2. Test ability to catch all errors\\n\" +\n"
//        + "      \"// ✅ 3. Test ability to re-direct console.log\\n\" +\n"
//        + "      \"// 4. Test all sandbox permissions are enforced\\n\" +\n"
//        + "      \"// 5. Test ability to import npm modules from inside a worker\");\n"
//        + "  console.log(\"payload: \", params);\n"
//        + "  return 17;\n"
//        + "}\n"
//        + "\n"
//        + "// Worker Boundary\n"
//        + "\n"
//        + "// 1. Test ability to send the result back\n"
//        + "// ✅ 2. Test ability to catch all errors\n"
//        + "// ✅ 3. Test ability to re-direct console.log\n"
//        + "// ✅ 4. Test all sandbox permissions are enforced\n"
//        + "// ✅ 5. Test ability to import npm modules from inside a worker\n"
//        + "\n"
//        + "self.onmessage = async (event) => {\n"
//        + "  const uid = event.data.uid;\n"
//        + "  try {\n"
//        + "    // Re-direct console.log statements\n"
//        + "    // TODO: may need to handle other console calls\n"
//        + "    console.log = (...args) => {\n"
//        + "      const message = args.map(it => JSON.stringify(it)).join(' ');\n"
//        + "      self.postMessage({ stdout: message, uid: uid });\n"
//        + "    }\n"
//        + "    const result = await handler(event.data.payload);\n"
//        + "    self.postMessage({ result: result, uid: uid });\n"
//        + "  } catch (e) {\n"
//        + "    self.postMessage({ error: e.message, uid: uid });\n"
//        + "  }\n"
//        + "  self.close();\n"
//        + "};";
  }

  private String workerScript(String rawCode, String interfaces) {
    StringJoiner joiner = new StringJoiner("\n");
    String entryPointPrefix = "export async function handler";
    String[] codeTokens = rawCode.split(entryPointPrefix);
    if (codeTokens.length > 0) {
      // Insert dto code in-between item 0 and item 1
      // Item 0 would be npm imports or other pieces of code
      // Item 1 would be the entry point
      if (codeTokens.length == 2) {
        joiner.add(codeTokens[0]);
        joiner.add(interfaces);
        joiner.add(entryPointPrefix + codeTokens[1]);
      } else if (codeTokens.length == 1) {
        joiner.add(interfaces);
        joiner.add(entryPointPrefix + codeTokens[0]);
      } else {
        throw new RuntimeException("There must be exactly one handler function define");
      }
    }

    try {
      File file = ResourceUtils.getFile("classpath:ts/workerTemplate.ts");
      String workerTemplate =  new String(Files.readAllBytes(file.toPath()));
      joiner.add(workerTemplate);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return joiner.toString();
  }

  @Override
  public GenericResponse handleExecResult(ExecResult execResult) {
    if (!ObjectUtils.isEmpty(execResult.getFcmToken()) && !ObjectUtils.isEmpty(execResult.getUid())) {
      String uid = parseUid(execResult.getUid());
      String stdout = String.join("\n", execResult.getStdOut()).replace("\\n", "\n");
      if (!ObjectUtils.isEmpty(stdout)) {
        log.info("{}: {}", uid, stdout);
      }
      if (!ObjectUtils.isEmpty(execResult.getError())) {
        log.error("{}: {}", uid, execResult.getError());
      }
      try {
        validateCodeCell(execResult, uid);
        Message.Builder builder = Message.builder();
        builder.putData("uid", uid);
        builder.putData("type", MessageType.EXEC_RESULT);
        if (!ObjectUtils.isEmpty(execResult.getResult())) {
          builder.putData("result", objectMapper.writeValueAsString(execResult.getResult()));
        }
        if (!ObjectUtils.isEmpty(execResult.getError())) {
          builder.putData("error", execResult.getError());
        }
        if (!ObjectUtils.isEmpty(stdout)) {
          builder.putData("std_out", stdout);
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

  private String parseUid(String uid) {
  }

  private void validateCodeCell(ExecResult execResult, String uid) {
    CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(uid));
    if (codeCell != null) {
      if (!ObjectUtils.isEmpty(execResult.getResult())) {
        codeCell.setIsDeployable(true);
        codeCell.setReasonNotDeployable(null);
        codeCellRepo.save(codeCell);
      } else if (!ObjectUtils.isEmpty(execResult.getError())) {
        codeCell.setIsDeployable(false);
        codeCell.setReasonNotDeployable("Functions with unresolved errors cannot be deployed");
        codeCellRepo.save(codeCell);
      }
    }
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
        codeCell.setInterfaces(code.getInterfaces());
        if (!ObjectUtils.isEmpty(code.getParentId())) {
          codeCell.setParentId(UUID.fromString(code.getParentId()));
        }
        codeCellRepo.save(codeCell);
        updatedCell = codeCell;
      }
      else {
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
            } else if (field.equals("interfaces")) {
              codeCell.setInterfaces(code.getInterfaces());
            }
          }
          codeCell.setDeployed(false);
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
            .interfaces(codeCell.getInterfaces())
            .description(codeCell.getDescription())
            .functionName(codeCell.getFunctionName())
            .updatedAt(codeCell.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
            .createdAt(codeCell.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
      }
    }
    return null;
  }

  @Override
  public void generateOpenApiSpec(String interfaces, String uid) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("file", interfaces);
    payload.put("env", sourceProps.getProfile());
    payload.put("uid", uid);
    submitExecutionTask(payload, tsToOaUrl);
//    return null;
//    String file = "export interface Author {\n"
//        + "  name: string;\n"
//        + "  image: string;\n"
//        + "  designation: string;\n"
//        + "};\n"
//        + "\n"
//        + "export interface Blog {\n"
//        + "  id: number;\n"
//        + "  title: string;\n"
//        + "  paragraph: string;\n"
//        + "  image: string;\n"
//        + "  author: Author;\n"
//        + "  tags: string[];\n"
//        + "  publishDate: string;\n"
//        + "};";
//
//    String ts = "export interface Author {\n"
//        + "  name: string;\n"
//        + "  image: string;\n"
//        + "  designation: string;\n"
//        + "};\n"
//        + "\n"
//        + "export interface ResponsePayload {\n"
//        + "  id: number;\n"
//        + "  title: string;\n"
//        + "  paragraph: string;\n"
//        + "  image: string;\n"
//        + "  author: Author;\n"
//        + "  tags: string[];\n"
//        + "  publishDate: string;\n"
//        + "};\n"
//        + "\n"
//        + "export interface RequestPayload {\n"
//        + "  id: number;\n"
//        + "  title: string;\n"
//        + "  paragraph: string;\n"
//        + "  image: string;\n"
//        + "  author: Author;\n"
//        + "  tags: string[];\n"
//        + "  publishDate: string;\n"
//        + "};\n"
//        + "\n"
//        + "export default async function(payload: RequestPayload) {\n"
//        + "  console.log(\"Hello, world!\")\n"
//        + "}";
//    String json = "{\n"
//        + "  \"openapi\": \"3.0.0\",\n"
//        + "  \"info\": {\n"
//        + "    \"title\": \"GPT Lambda API\",\n"
//        + "    \"version\": \"v1\",\n"
//        + "    \"x-comment\": \"Generated by core-types-json-schema (https://github.com/grantila/core-types-json-schema) on behalf of typeconv (https://github.com/grantila/typeconv)\"\n"
//        + "  },\n"
//        + "  \"paths\": {},\n"
//        + "  \"components\": {\n"
//        + "    \"schemas\": {\n"
//        + "      \"Author\": {\n"
//        + "        \"properties\": {\n"
//        + "          \"name\": {\n"
//        + "            \"description\": \"Author.name\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"image\": {\n"
//        + "            \"description\": \"Author.image\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"designation\": {\n"
//        + "            \"description\": \"Author.designation\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          }\n"
//        + "        },\n"
//        + "        \"required\": [\n"
//        + "          \"name\",\n"
//        + "          \"image\",\n"
//        + "          \"designation\"\n"
//        + "        ],\n"
//        + "        \"additionalProperties\": false,\n"
//        + "        \"description\": \"Author\",\n"
//        + "        \"type\": \"object\"\n"
//        + "      },\n"
//        + "      \"ResponsePayload\": {\n"
//        + "        \"properties\": {\n"
//        + "          \"id\": {\n"
//        + "            \"description\": \"ResponsePayload.id\",\n"
//        + "            \"type\": \"number\"\n"
//        + "          },\n"
//        + "          \"title\": {\n"
//        + "            \"description\": \"ResponsePayload.title\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"paragraph\": {\n"
//        + "            \"description\": \"ResponsePayload.paragraph\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"image\": {\n"
//        + "            \"description\": \"ResponsePayload.image\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"author\": {\n"
//        + "            \"$ref\": \"#/components/schemas/Author\",\n"
//        + "            \"description\": \"ResponsePayload.author\"\n"
//        + "          },\n"
//        + "          \"tags\": {\n"
//        + "            \"items\": {\n"
//        + "              \"description\": \"ResponsePayload.tags.[]\",\n"
//        + "              \"type\": \"string\"\n"
//        + "            },\n"
//        + "            \"description\": \"ResponsePayload.tags\",\n"
//        + "            \"type\": \"array\"\n"
//        + "          },\n"
//        + "          \"publishDate\": {\n"
//        + "            \"description\": \"ResponsePayload.publishDate\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          }\n"
//        + "        },\n"
//        + "        \"required\": [\n"
//        + "          \"id\",\n"
//        + "          \"title\",\n"
//        + "          \"paragraph\",\n"
//        + "          \"image\",\n"
//        + "          \"author\",\n"
//        + "          \"tags\",\n"
//        + "          \"publishDate\"\n"
//        + "        ],\n"
//        + "        \"additionalProperties\": false,\n"
//        + "        \"description\": \"ResponsePayload\",\n"
//        + "        \"type\": \"object\"\n"
//        + "      },\n"
//        + "      \"RequestPayload\": {\n"
//        + "        \"properties\": {\n"
//        + "          \"id\": {\n"
//        + "            \"description\": \"RequestPayload.id\",\n"
//        + "            \"type\": \"number\"\n"
//        + "          },\n"
//        + "          \"title\": {\n"
//        + "            \"description\": \"RequestPayload.title\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"paragraph\": {\n"
//        + "            \"description\": \"RequestPayload.paragraph\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"image\": {\n"
//        + "            \"description\": \"RequestPayload.image\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          },\n"
//        + "          \"author\": {\n"
//        + "            \"$ref\": \"#/components/schemas/Author\",\n"
//        + "            \"description\": \"RequestPayload.author\"\n"
//        + "          },\n"
//        + "          \"tags\": {\n"
//        + "            \"items\": {\n"
//        + "              \"description\": \"RequestPayload.tags.[]\",\n"
//        + "              \"type\": \"string\"\n"
//        + "            },\n"
//        + "            \"description\": \"RequestPayload.tags\",\n"
//        + "            \"type\": \"array\"\n"
//        + "          },\n"
//        + "          \"publishDate\": {\n"
//        + "            \"description\": \"RequestPayload.publishDate\",\n"
//        + "            \"type\": \"string\"\n"
//        + "          }\n"
//        + "        },\n"
//        + "        \"required\": [\n"
//        + "          \"id\",\n"
//        + "          \"title\",\n"
//        + "          \"paragraph\",\n"
//        + "          \"image\",\n"
//        + "          \"author\",\n"
//        + "          \"tags\",\n"
//        + "          \"publishDate\"\n"
//        + "        ],\n"
//        + "        \"additionalProperties\": false,\n"
//        + "        \"description\": \"RequestPayload\",\n"
//        + "        \"type\": \"object\"\n"
//        + "      }\n"
//        + "    }\n"
//        + "  }\n"
//        + "}";
  }

  @Override
  public GenericResponse handleSpecResult(SpecResult specResult) {
    String spec = specResult.getSpec().getValue();
    String format = specResult.getSpec().getFormat();
    log.info("Spec: {}", spec);
    if (format.equals("ts")) {
      spec = spec.replace("/**", "\n/**");
    }
    if (!ObjectUtils.isEmpty(specResult.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(specResult.getUid()));
      if (codeCell != null) {
        if (format.equals("json")) {
          codeCell.setOpenApiSchema(spec);
        } else if (format.equals("ts")) {
          codeCell.setInterfaces(Base64.getEncoder().encodeToString(spec.getBytes()));
        }
        codeCellRepo.save(codeCell);
      }
    }
    return new GenericResponse().status("ok");
  }

  @Override
  public GenericResponse deploy(ExecRequest execRequest) {
    if (!ObjectUtils.isEmpty(execRequest.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(execRequest.getUid()));
      if (codeCell != null) {
        if (codeCell.getIsDeployable()) {
          generateOpenApiSpec(codeCell.getInterfaces(), codeCell.getUid().toString());
          codeCell.setDeployed(true);
          codeCellRepo.save(codeCell);
          return new GenericResponse().status("ok");
        } else {
          return new GenericResponse().error(codeCell.getReasonNotDeployable());
        }
      }
    }
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
