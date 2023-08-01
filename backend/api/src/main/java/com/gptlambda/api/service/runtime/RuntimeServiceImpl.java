package com.gptlambda.api.service.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gptlambda.api.Code;
import com.gptlambda.api.CodeUpdateResponse;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResultAsync;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.SpecResult;
import com.gptlambda.api.data.postgres.entity.CodeCellEntity;
import com.gptlambda.api.data.postgres.entity.CommitHistoryEntity;
import com.gptlambda.api.data.postgres.entity.EntitlementEntity;
import com.gptlambda.api.data.postgres.repo.CodeCellRepo;
import com.gptlambda.api.data.postgres.repo.CommitHistoryRepo;
import com.gptlambda.api.data.postgres.repo.EntitlementRepo;
import com.gptlambda.api.dto.ExecRequestPayload;
import com.gptlambda.api.dto.GenerateSpecRequest;
import com.gptlambda.api.props.DenoProps;
import com.gptlambda.api.props.SourceProps;
import com.gptlambda.api.service.utils.GPTLambdaUtils;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
  private final EntitlementRepo entitlementRepo;
  private final CommitHistoryRepo commitHistoryRepo;
  private final Slugify slugify;
  private final DenoProps denoProps;
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

  // Define a unique version key to avoid conflicts
  public final static String versionKey = "version_" + GPTLambdaUtils.generateUid(6);

  public static final class MessageType {
    public static final String EXEC_RESULT = "EXEC_RESULT";
    public static final String CHAT = "CHAT";
    public static final String HEART_BEAT = "HEART_BEAT";
    public static final String PRODUCT_INIT = "PRODUCT_INIT";
    public static final String FCM_TOKEN = "FCM_TOKEN";
    public static final String PRODUCT_INFO_REQUEST = "PRODUCT_INFO_REQUEST";
  }

  @Override
  public ExecResultAsync getExecutionResult(final String execId) {
    // TODO: We should probably use a redis for this
    Future<ExecResultAsync> task = executorService.submit(() -> {
      ExecResultAsync result = null;
      while (result == null) {
        result = executionResults.get(execId);
        if (result == null) {
          Thread.sleep(10);
        }
      }
      executionResults.remove(execId);
      return result;
    });
    try {
      return task.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("{}.handleSpecResult: {}",
          getClass().getSimpleName(),
          e.getMessage());
    }
    return new ExecResultAsync();
  }


  @Override
  public GenericResponse exec(ExecRequest execRequest) {
    if (!ObjectUtils.isEmpty(execRequest.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(execRequest.getUid()));
      if (codeCell != null) {
        EntitlementEntity entitlements = entitlementRepo.findByUserId(codeCell.getUserId());
        String version = codeCell.getVersion();
        ExecRequestPayload request = new ExecRequestPayload();
        request.setPayload(execRequest.getPayload());
        request.setEnv(sourceProps.getProfile());
        request.setUid(execRequest.getUid() + "@" +version);
        request.setFcmToken(execRequest.getFcmToken());
        request.setTimeout(entitlements.getTimeout());
        request.setValidate(execRequest.getValidate());
        request.setExecId(execRequest.getExecId());
        Thread.startVirtualThread(() -> submitExecutionTask(request, getRuntimeUrl()));
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
        String rawCode = "";
        if (!ObjectUtils.isEmpty(entity.getCode())) {
          rawCode = new String(Base64.getDecoder().decode(entity.getCode().getBytes()));
        }
        String interfaces = "";
        if (!ObjectUtils.isEmpty(entity.getInterfaces())) {
          interfaces = new String(Base64.getDecoder().decode(entity.getInterfaces().getBytes()));
        }
        return workerScript(rawCode, interfaces);
      }
    }
    return null;
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
  public GenericResponse handleExecResult(ExecResultAsync execResult) {
    if (!ObjectUtils.isEmpty(execResult.getFcmToken()) && !ObjectUtils.isEmpty(execResult.getUid())) {
      // TODO: No need to validate code in active use by gpt
      ExecResultAsync execResultAsync = new ExecResultAsync();
      String uid = parseUid(execResult.getUid());
      if (execResult.getValidate() != null && execResult.getValidate()) {
        Thread.startVirtualThread(() -> validateCodeCell(execResult, uid));
      }
      execResultAsync.setUid(uid);
      execResultAsync.setExecId(execResult.getExecId());
      execResultAsync.setFcmToken(execResult.getFcmToken());
      Message.Builder builder = Message.builder();
      builder.putData("uid", uid);
      builder.putData("type", MessageType.EXEC_RESULT);
      if (!ObjectUtils.isEmpty(execResult.getResult())) {
        String o = secureString(new Gson().toJson(execResult.getResult()));
        builder.putData("result", o);
        execResultAsync.setResult(o);
        log.info("{} result: {}", uid, o);
      }
      if (!ObjectUtils.isEmpty(execResult.getError())) {
        String error = secureString(execResult.getError());
        log.error("{} error: {}", uid, error);
        builder.putData("error", error);
        execResultAsync.setError(error);
      }
      if (!ObjectUtils.isEmpty(execResult.getStdOut())) {
        String stdout = secureString(String.join("\n", execResult.getStdOut())
            .replace("\\n", "\n"));
        log.info("{} stdout: {}", uid, stdout);
        builder.putData("std_out", stdout);
        execResultAsync.setStdOutStr(stdout);
      }
      executionResults.put(execResult.getExecId(), execResultAsync);
      Message message = builder.setToken(execResult.getFcmToken()).build();
      sendFcmMessage(message);
    }
    return new GenericResponse().status("ok");
  }

  private String secureString(String s) {
    // Remove any references to internal directories or state
    return s
        .replace("deno", "...")
        .replace("Deno", "...")
        .replace("http://host.docker.internal:8080", "node_modules")
        .replace(getCodeGenUrl(), "node_modules")
        .replace(getRuntimeUrl(), "node_modules");
  }

  private String parseUid(String uid) {
    return uid.split("@")[0];
  }

  private void validateCodeCell(ExecResultAsync execResult, String uid) {
    CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(uid));
    if (codeCell != null) {
      String error = null;
      // TODO: ensure function name is unique within the user's namespace
      if (ObjectUtils.isEmpty(codeCell.getFunctionName())) {
        error = "Please provide a function name and description";
      } else if (ObjectUtils.isEmpty(codeCell.getJsonSchema())) {
        error = "Missing request/response interface definitions";
      } else {
        if (!ObjectUtils.isEmpty(execResult.getResult())) {
          codeCell.setIsDeployable(true);
          codeCell.setReasonNotDeployable(null);
          codeCellRepo.save(codeCell);
        } else if (!ObjectUtils.isEmpty(execResult.getError())) {
          error = "Functions with unresolved errors cannot be deployed";
        } else {
          error = "Your function must have a return value";
        }
      }
      if (error != null) {
        codeCell.setIsDeployable(false);
        codeCell.setReasonNotDeployable(error);
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
    String rawCode = null;
    CodeCellEntity updatedCell = null;
    if (!ObjectUtils.isEmpty(userId)) {
      if (ObjectUtils.isEmpty(code.getUid())) {
        if (!ObjectUtils.isEmpty(code.getCode())) {
          rawCode = new String(Base64.getDecoder().decode(code.getCode().getBytes()));
        }
        CodeCellEntity codeCell = new CodeCellEntity();
        codeCell.setUid(UUID.randomUUID());
        codeCell.setCode(code.getCode());
        codeCell.setDescription(parseCodeComment(rawCode, "@summary"));
        codeCell.setUserId(userId);
        codeCell.setIsActive(isBelowActiveLimit(code.getUserId()));
        codeCell.setIsPublic(false);
        codeCell.setFunctionName(parseCodeComment(rawCode, "@name"));
        codeCell.setSlug(slugify.toSlug(codeCell.getFunctionName()));
        codeCell.setVersion(generateCodeVersion());
        if (!ObjectUtils.isEmpty(code.getParentId())) {
          codeCell.setParentId(UUID.fromString(code.getParentId()));
        }
        codeCell.setDeployed(false);
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
              rawCode = new String(Base64.getDecoder().decode(code.getCode().getBytes()));
              codeCell.setDescription(parseCodeComment(rawCode, "@summary"));
              codeCell.setFunctionName(parseCodeComment(rawCode, "@name"));
              codeCell.setSlug(slugify.toSlug(codeCell.getFunctionName()));
            }
            else if (field.equals("is_active")) {
              if (isBelowActiveLimit(userId) || !code.getIsActive()) {
                codeCell.setIsActive(code.getIsActive());
              }
            } else if (field.equals("is_public")) {
              codeCell.setIsPublic(code.getIsPublic());
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
        final CodeCellEntity cell = updatedCell;
        Thread.startVirtualThread(() -> generateJsonSchema(
            new String(Base64.getDecoder()
                .decode(cell.getCode()
                    .getBytes())), cell.getUid()
                .toString()));
        return new CodeUpdateResponse().uid(updatedCell.getUid().toString());
      }
    }
    return new CodeUpdateResponse();
  }

  private String parseCodeComment(String rawCode, String property) {
    StringJoiner joiner = new StringJoiner(" ");
    if (!ObjectUtils.isEmpty(rawCode)) {
      List<String> lines = new ArrayList<>(
          List.of(rawCode
          .replace("/", "")
          .replace("*", "").split("\n")))
          .stream().map(it -> it
              .trim()
              .strip()).toList();
      boolean propertyStart = false, propertyEnd = false;
      for (String line : lines) {
        if (line.startsWith(property)) {
          propertyStart = true;
        }
        if (propertyStart && line.startsWith("@") && !line.startsWith(property)) {
          propertyEnd = true;
        }
        if (propertyStart && !propertyEnd) {
          joiner.add(line.replace(property, "")
              .replace("\n", "")
              .strip());
        }
      }
    }
    return joiner.toString();
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
            .updatedAt(codeCell.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
            .createdAt(codeCell.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
      }
    }
    return null;
  }

  @Override
  public void generateJsonSchema(String code, String uid) {
    GenerateSpecRequest request = new GenerateSpecRequest();
    request.setFile(code);
    request.setEnv(sourceProps.getProfile());
    request.setUid(uid);
    request.setFrom("ts");
    request.setTo("jsc");
    submitExecutionTask(request, getCodeGenUrl());
  }

  @Override
  public String getJsonSchema(String uid) {
    String schema = jsonSchema.get(uid);
    if (schema != null) {
      jsonSchema.remove(uid);
    }
    return schema;
  }

  @Override
  public GenericResponse handleSpecResult(SpecResult specResult) {
    String spec = specResult.getSpec().getValue();
    String format = specResult.getSpec().getFormat();
    if (format.equals("ts")) {
      spec = spec.replace("/**", "\n/**");
    }
    if (!ObjectUtils.isEmpty(specResult.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(specResult.getUid()));
      if (codeCell != null) {
        if (format.equals("json")) {
          Map<String, Object> requestDto  = injectVersion(constructDto(spec), codeCell.getVersion());
          String requestDtoStr = new Gson().toJson(requestDto);
          codeCell.setJsonSchema(requestDtoStr);
          jsonSchema.put(codeCell.getUid().toString(), requestDtoStr);
        } else if (format.equals("ts")) {
          // TODO: this field is not used for anything so may remove it. We'll never need to convert
          // to TypeScript
          codeCell.setInterfaces(Base64.getEncoder().encodeToString(spec.getBytes()));
        }
        codeCell.setUpdatedAt(LocalDateTime.now());
        codeCellRepo.save(codeCell);
        return new GenericResponse().status("ok");
      }
    }
    return new GenericResponse().error("Something went wrong");
  }

  private Map<String, Object> injectVersion(Map<String, Object> requestDto, String version) {
    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
    TypeReference<List<String>> listTypeRef = new TypeReference<>() {};
    try {
      Map<String, Object> modifiedDto = objectMapper.readValue(
          new Gson().toJson(requestDto.get("RequestEntity")),
          typeRef);
      List<String> required = objectMapper.readValue(new Gson().toJson(modifiedDto.get("required")),
          listTypeRef);
      Map<String, Object> properties = objectMapper.readValue(new Gson().toJson(modifiedDto.get("properties")),
          typeRef);
      Map<String, Object> field = new HashMap<>();
      field.put("type", "string");
      field.put("default", version);
      properties.put(versionKey, field);
      required.add(versionKey);
      modifiedDto.put("required", required);
      modifiedDto.put("properties", properties);
      return modifiedDto;
    } catch (JsonProcessingException e) {
      log.error("{}.handleSpecResult: {}",
          getClass().getSimpleName(),
          e.getMessage());
    }
    return requestDto;

  }

  private Map<String, Object> constructDto(String spec) {
    try {
      Map<String, Object> cleanObjects = new HashMap<>();
      Map<String, Object> lookupObjects = new HashMap<>();
      Map<String, Object> mergedObject = new HashMap<>();
      Set<String> refs = new HashSet<>();
      JsonNode node = objectMapper.readValue(spec, JsonNode.class)
          .get("definitions");
      JsonObject jsonObject = JsonParser.parseString(
              objectMapper.writeValueAsString(node))
          .getAsJsonObject();
      walkSchema(jsonObject, cleanObjects, refs, lookupObjects);
      if (refs.size() > 0) {
        jsonObject = JsonParser.parseString(
                new Gson().toJson(cleanObjects))
            .getAsJsonObject();
        mergeRefs(jsonObject, mergedObject, lookupObjects);
      }
      return mergedObject;
    } catch (IOException e) {
      log.error("{}.handleSpecResult: {}",
          getClass().getSimpleName(),
          e.getMessage());
    }
    return new HashMap<>();
  }

  private void mergeRefs(JsonObject schema, Map<String, Object> objects,
      Map<String, Object> lookupObjects) {
    schema.keySet().forEach(key -> {
      Object value = schema.get(key);
      if (key.equals("$ref")) {
        value = lookupObjects.get(schema.get(key).getAsString());
        value = JsonParser.parseString(
                new Gson().toJson(value))
            .getAsJsonObject();
      }
      if (value instanceof JsonObject childValue) {
        Map<String, Object> currentObject = new HashMap<>();
        mergeRefs(childValue, currentObject, lookupObjects);
        if (key.equals("$ref")) {
          objects.putAll(currentObject);
        } else {
          objects.put(key, currentObject);
        }
      } else  {
        objects.put(key, value);
      }
    });
  }

  private void walkSchema(JsonObject schema, Map<String, Object> objects, Set<String> refs,
      Map<String, Object> lookupObjects) {
    schema.keySet().forEach(key -> {
      Object value = schema.get(key);
      if (key.equals("$ref")) {
        refs.add(value.toString());
      }
      if (value instanceof JsonObject childValue) {
        Map<String, Object> currentObject = new HashMap<>();
        if (childValue.get("type") != null && childValue.get("type").toString()
            .replace("\"","")
            .equals("object") || childValue.get("enum") != null) {
          lookupObjects.put("#/definitions/" + key, currentObject);
        }
        walkSchema(childValue, currentObject, refs, lookupObjects);
        objects.put(key, currentObject);
      } else if (!key.equals("additionalProperties"))  {
        objects.put(key, value);
      }
    });
  }

  @Override
  public GenericResponse deploy(ExecRequest execRequest) {
    if (!ObjectUtils.isEmpty(execRequest.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(execRequest.getUid()));
      if (codeCell != null) {
        if (codeCell.getIsDeployable()) {
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

  private Boolean isBelowActiveLimit(String userId) {
    return codeCellRepo.numActiveCells(userId) < Integer.MAX_VALUE;
  }

  private void sendFcmMessage(Message message) {
    try {
      FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error("{}.sendFcmMessage: {}",
          getClass().getSimpleName(),
          e.getMessage());
    }
  }

  private void submitExecutionTask(Object body, String url) {
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

  private StringEntity getEntityBody(Object body) {
    try {
      return new StringEntity(new Gson().toJson(body));
    } catch (UnsupportedEncodingException e) {
      log.error("{}.getEntityBody: {}",
          getClass().getSimpleName(),
          e.getMessage());
    }
    return null;
  }

  private String getRuntimeUrl() {
    return String.format("%s%s",
        denoProps.getRuntime().getUrl(),
        denoProps.getRuntime().getPath());
  }


  private String getCodeGenUrl() {
    return String.format("%s%s",
        denoProps.getInternal().getUrl(),
        denoProps.getInternal().getPath());
  }
}
