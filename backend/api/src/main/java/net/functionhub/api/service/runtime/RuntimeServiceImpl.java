package net.functionhub.api.service.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.ExecResultAsync;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.SpecResult;
import net.functionhub.api.StatusRequest;
import net.functionhub.api.StatusResponse;
import net.functionhub.api.UserProfile;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.CommitHistoryEntity;
import net.functionhub.api.data.postgres.entity.EntitlementEntity;
import net.functionhub.api.data.postgres.projection.Deployment;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.CommitHistoryRepo;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.dto.ExecRequestInternal;
import net.functionhub.api.dto.GenerateSpecRequest;
import net.functionhub.api.dto.RequestHeaders;
import net.functionhub.api.props.DenoProps;
import net.functionhub.api.props.SourceProps;
import net.functionhub.api.service.user.UserService.AuthMode;
import net.functionhub.api.service.utils.FHUtils;
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
import net.functionhub.api.service.utils.SpecTemplate;
import net.functionhub.api.service.utils.WordList;
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
  private final DenoProps denoProps;
  private final WordList wordList;
  private final SpecTemplate specTemplate;
  private final RequestHeaders requestHeaders;
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  private final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};

  // Define a unique version key to avoid conflicts
  public final static String versionKey = "version_" + FHUtils.generateUid(6);
  private final String deployedFlag = "+deployed";

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
  public ExecResultAsync exec(ExecRequest execRequest) {
    if (!ObjectUtils.isEmpty(execRequest.getUid())) {
      return execHelper(execRequest, codeCellRepo.findByUid(UUID.fromString(execRequest.getUid())));
    }
    return new ExecResultAsync().error("Unknown error");
  }

  @Override
  public String runProdFunction(String functionSlug, String body) {
    UserProfile user = FHUtils.getSessionUser();
    if (!user.getAuthMode().equals(AuthMode.AK.name())) {
      throw new RuntimeException("Unsupported authentication mechanism");
    }
    return runFunctionHelper(functionSlug, body, true, false);
  }

  @Override
  public String runDevFunction(String functionSlug, String body) {
    UserProfile user = FHUtils.getSessionUser();
    if (!user.getAuthMode().equals(AuthMode.FB.name())) {
      throw new RuntimeException("Unsupported authentication mechanism");
    }
    return runFunctionHelper(functionSlug, body, false, true);
  }

  private String runFunctionHelper(String functionSlug, String body, boolean deployed, boolean validate) {
    // For development run, the latest version of the code is the one that should run
    // as that is the one the user is testing. For prod, it's the latest deployed version that runs.
    // Although there may be previous deployments in the commit history, only the deployment version
    // in CodeCellEntity is used.
    CodeCellEntity codeCell = codeCellRepo.findBySlugAndApiKey(
        functionSlug, FHUtils.getSessionUser().getApiKey());
    if (codeCell != null) {
      ExecRequest request = new ExecRequest()
          .execId(UUID.randomUUID().toString())
          .deployed(deployed)
          .validate(validate)
          .payload(body)
          .version(codeCell.getVersion())
          .uid(codeCell.getUid().toString());
      ExecResultAsync result = execHelper(request, codeCell);
      if (!ObjectUtils.isEmpty(result.getError())) {
        throw new RuntimeException(result.getError());
      }
      // TODO: inject the std out into the result for dev and ignore it for prod
      return JsonParser.parseString(result.getResult()).getAsString();
    }
    throw new RuntimeException("Requested service not found");

  }

  private ExecResultAsync execHelper(ExecRequest execRequest, CodeCellEntity codeCell) {
    if (!ObjectUtils.isEmpty(execRequest.getUid())) {
      String execId = execRequest.getExecId();
      if (ObjectUtils.isEmpty(execId)) {
        execId = UUID.randomUUID().toString();
      }
      if (codeCell != null) {
        EntitlementEntity entitlements = entitlementRepo.findByUserId(codeCell.getUserId());
        String version = codeCell.getVersion();
        if (!ObjectUtils.isEmpty(execRequest.getVersion())) {
          version = execRequest.getVersion();
        }
        String uid = execRequest.getUid() + "@" +version;
        if (execRequest.getDeployed() != null && execRequest.getDeployed()) {
          uid += deployedFlag;
        }
        ExecRequestInternal request = new ExecRequestInternal();
        request.setPayload(parseExecRequestPayload(execRequest.getPayload()));
        request.setEnv(sourceProps.getProfile());
        request.setUid(uid);
        request.setTimeout(entitlements.getTimeout());
        request.setValidate(execRequest.getValidate());
        request.setExecId(execId);
        request.setDeployed(execRequest.getDeployed());
        request.setVersion(version);
        Thread.startVirtualThread(() -> submitExecutionTask(request, getRuntimeUrl()));
      }
      return getExecutionResult(execId);
    }
    return new ExecResultAsync().error("Unknown error");
  }

  private Map<String, Object> parseExecRequestPayload(String payload) {
    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
    try {
      // TODO: include console logs for dev response
      return objectMapper.readValue(
          JsonParser.parseString(payload).getAsJsonObject().toString(), typeRef);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public String getUserCode(String uid) {
    boolean deployed = false;
    String version = null;
    if (uid != null) {
      if (uid.contains(deployedFlag)) {
        uid = uid.replace(deployedFlag, "");
        deployed = true;
      }
      if (uid.contains("@")) {
        version = parseVersion(uid);
      }
      uid = parseUid(uid);
      String code = null;
      if (deployed && !ObjectUtils.isEmpty(version)) {
        List<CommitHistoryEntity> commitHistoryEntities = commitHistoryRepo
            .findByCodeCellIdAndVersion(UUID.fromString(uid), version);
        code = commitHistoryEntities.get(0).getCode();
      } else {
        CodeCellEntity entity = codeCellRepo.findByUid(UUID.fromString(uid));
        code = entity.getCode();
      }
      if (!ObjectUtils.isEmpty(code)) {
        String rawCode = new String(Base64.getDecoder().decode(code.getBytes()));
        return workerScript(rawCode);
      }
    }
    return null;
  }

  private String workerScript(String rawCode) {
    StringJoiner joiner = new StringJoiner("\n");
    joiner.add(rawCode);
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
    if (!ObjectUtils.isEmpty(execResult.getUid())) {
      ExecResultAsync execResultAsync = new ExecResultAsync();
      String uid = parseUid(execResult.getUid());
      if (execResult.getValidate() != null && execResult.getValidate()) {
        Thread.startVirtualThread(() -> validateCodeCell(execResult, uid));
      }
      execResultAsync.setUid(uid);
      execResultAsync.setExecId(execResult.getExecId());

      if (!execResult.getResult().equals("null") &&
          !ObjectUtils.isEmpty(execResult.getResult())) {
        String o = secureString(new Gson().toJson(execResult.getResult()));
        execResultAsync.setResult(o);
      }
      if (!ObjectUtils.isEmpty(execResult.getError())) {
        String error = secureString(execResult.getError());
        execResultAsync.setError(error);
      }
      if (!ObjectUtils.isEmpty(execResult.getStdOut())) {
        String stdout = secureString(String.join("\n", execResult.getStdOut())
            .replace("\\n", "\n"));
        execResultAsync.setStdOutStr(stdout);
      }
      executionResults.put(execResult.getExecId(), execResultAsync);
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

  private String parseVersion(String uid) {
    return uid.split("@")[1];
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
        if (!ObjectUtils.isEmpty(execResult.getError()) && !execResult.getError().equals("null")) {
          error = execResult.getError();
        } else if (!ObjectUtils.isEmpty(execResult.getResult()) && !execResult.getResult().equals("null")) {
          codeCell.setIsDeployable(true);
          codeCell.setReasonNotDeployable(null);
          codeCellRepo.save(codeCell);
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
    return FHUtils.generateUid(FHUtils.LONG_UID_LENGTH);
  }

  @Override
  public CodeUpdateResult updateCode(Code code) {
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
        codeCell.setSlug(getUniqueSlug());
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
        // Reset schema fields
        updatedCell.setFullOpenApiSchema(null);
        updatedCell.setJsonSchema(null);

        final CodeCellEntity finalCell = updatedCell;
        Thread.startVirtualThread(() -> {
          CommitHistoryEntity commitHistory = new CommitHistoryEntity();
          commitHistory.setUid(UUID.randomUUID());
          commitHistory.setUserId(finalCell.getUserId());
          commitHistory.setCodeCellId(finalCell.getUid());
          commitHistory.setVersion(finalCell.getVersion());
          commitHistory.setCode(finalCell.getCode());
          commitHistoryRepo.save(commitHistory);
        });

        Thread.startVirtualThread(() -> generateJsonSchema(
            new String(Base64.getDecoder()
                .decode(finalCell.getCode()
                    .getBytes())), finalCell.getUid()
                .toString()));
        return new CodeUpdateResult()
            .uid(updatedCell.getUid().toString())
            .slug(updatedCell.getSlug())
            .version(updatedCell.getVersion());
      }
    }
    return new CodeUpdateResult();
  }

  private String getUniqueSlug() {
    int numTries = 5;
    int wordLength = 6;
    String slug = wordList.getRandomPhrase(wordLength);
    while (numTries > 0 && codeCellRepo.findBySlug(slug) != null) {
      slug = wordList.getRandomPhrase(3);
      numTries--;
    }
    if (numTries < 0) {
      // If we've exhausted the namespace of all possible slugs (~10^9), then just generate
      // a UUID. Even though it's ugly, it will prevent conflicts. If this ever happens,
      // we should increase the word length.
      return UUID.randomUUID().toString();
    }
    return slug;
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
    String spec = null;
    String format = "json";
    if (!ObjectUtils.isEmpty(specResult.getSpec())) {
      spec = specResult.getSpec().getValue();
      format = specResult.getSpec().getFormat();
    }
    if (format.equals("ts")) {
      spec = spec.replace("/**", "\n/**");
    }
    if (!ObjectUtils.isEmpty(specResult.getUid())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(specResult.getUid()));
      if (codeCell != null) {
        if (format.equals("json")) {
          Map<String, Object> requestDto  = getRequestDto( constructDto(spec), codeCell.getVersion());
          String requestDtoStr = new Gson().toJson(requestDto);
          codeCell.setJsonSchema(requestDtoStr);
          String fullOpenApiSchema = generateFullSpec(spec, specResult.getUid());
          codeCell.setFullOpenApiSchema(fullOpenApiSchema);
          jsonSchema.put(codeCell.getUid().toString(), requestDtoStr);

          // Insert the schema into an existing commit
          List<CommitHistoryEntity> commitHistory = commitHistoryRepo.findByCodeCellIdAndVersion(
              codeCell.getUid(), codeCell.getVersion()
          );
          if (!ObjectUtils.isEmpty(commitHistory)) {
            commitHistory.get(0).setJsonSchema(requestDtoStr);
            commitHistory.get(0).setFullOpenApiSchema(fullOpenApiSchema);
            commitHistoryRepo.save(commitHistory.get(0));
          }
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

  private String generateFullSpec(String spec, String uid) {
    Map<String, Object> fullSpecMap = null;
    try {
      spec = spec.replace("#/definitions", "#/components/definitions");
      fullSpecMap = objectMapper.readValue(spec,
          typeRef);
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(uid));
      if (codeCell != null) {
        Map<String, Object> template = specTemplate.getUserSpec();
        Map<String, Object> paths = new HashMap<>();

        // Define a custom path for this user's function
        Map<String, Object> pathTemplate = objectMapper.readValue(
            new Gson().toJson(template.get("paths")), typeRef);
        paths.put("/" + codeCell.getSlug(), pathTemplate.get("/"));
        template.put("paths", paths);
        fullSpecMap.remove("$comment");
        template.put("components", fullSpecMap);
        return new Gson().toJson(template);
      }

    } catch (JsonProcessingException e) {
      log.error("{}.handleSpecResult: {}",
          getClass().getSimpleName(),
          e.getMessage());
    }
    return new Gson().toJson(specTemplate.getUserSpec());
  }

  private Map<String, Object> getRequestDto(Map<String, Object> requestDto, String version) {
    TypeReference<List<String>> listTypeRef = new TypeReference<>() {};
    try {
      Map<String, Object> modifiedDto = objectMapper.readValue(
          new Gson().toJson(requestDto.get("RequestEntity")),
          typeRef);
      if (modifiedDto != null) {
        List<String> required = objectMapper.readValue(
            new Gson().toJson(modifiedDto.get("required")),
            listTypeRef);
        Map<String, Object> properties = objectMapper.readValue(
            new Gson().toJson(modifiedDto.get("properties")),
            typeRef);
//        Map<String, Object> field = new HashMap<>();
//        field.put("type", "string");
//        field.put("default", version);
//        properties.put(versionKey, field);
//        required.add(versionKey);
        modifiedDto.put("required", required);
        modifiedDto.put("properties", properties);
        return modifiedDto;
      }
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
      return mergedObject.size() > 0 ? mergedObject : cleanObjects;
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
          codeCell.setDeployedVersion(codeCell.getVersion());
          codeCellRepo.save(codeCell);
          List<CommitHistoryEntity> commitHistory = commitHistoryRepo.findByCodeCellIdAndVersion(
              codeCell.getUid(),
              codeCell.getVersion());
          if (!ObjectUtils.isEmpty(commitHistory)) {
            commitHistory.get(0).setDeployed(true);
            commitHistoryRepo.save(commitHistory.get(0));
          }
          return new GenericResponse().status("ok");
        } else {
          return new GenericResponse().error(codeCell.getReasonNotDeployable());
        }
      }
    }
    return new GenericResponse().error("No previous commits found");
  }

  @Override
  public String getUserSpec(String functionId, String version, String env) {
    if (!ObjectUtils.isEmpty(functionId) &&
        !ObjectUtils.isEmpty(version) &&
        !ObjectUtils.isEmpty(env)) {
      // todo: apply ownership check to deployed specs
      if (env.equals("fhd")) {
        // Dev
        CodeCellEntity codeCell = codeCellRepo.findBySlugAndVersion(functionId, version);
        if (codeCell == null || !codeOwner(codeCell)) {
          throw new RuntimeException("Invalid request");
        }
        if (ObjectUtils.isEmpty(codeCell.getFullOpenApiSchema())) {
          throw new RuntimeException("Requested function not available");
        }

        try {
          Map<String, Object> spec = objectMapper.readValue(codeCell.getFullOpenApiSchema(), typeRef);
          Map<String, Object> prodPaths = objectMapper.readValue( new Gson().toJson(spec.get("paths")),
              typeRef);
          String key = "/" + codeCell.getSlug();
          Map<String, Object> devPaths = new HashMap<>();
          devPaths.put("/d" + key, prodPaths.get(key));
          spec.put("paths", devPaths);
          return new Gson().toJson(spec);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Error processing api specification");
        }

      } else if (env.equals("fhp")) {
        // Prod
        Deployment deployment = commitHistoryRepo.findDeployedCommitByVersionAndSlug(version, functionId);
        if (deployment == null) {
          throw new RuntimeException("A deployed function does not exist for " + functionId);
        }
        return deployment.getSchema();
      } else if (env.equals("gd")) {
        // GPT dev
        // TODO: Load the function-specific gpt dev spec
        Map<String, Object> gptDevSpec = specTemplate.getGptDevSpec();
        Map<String, Object> paths = new HashMap<>();

        try {
          Map<String, Object> pathTemplate = objectMapper.readValue(
              new Gson().toJson(gptDevSpec.get("paths")), typeRef);
          paths.put("/completion/" + functionId, pathTemplate.get("/completion"));
          gptDevSpec.put("paths", paths);
          return new Gson().toJson(gptDevSpec);
        } catch (JsonProcessingException e) {
          log.error("{}.submitExecutionTask: {}",
              getClass().getSimpleName(),
              e.getMessage());
        }
      } else if (env.equals("gp")) {
       return new Gson().toJson(specTemplate.getGptProdSpec());
      }
    }
    throw new RuntimeException("Invalid request");
  }

  @Override
  public StatusResponse getSpecStatus(StatusRequest statusRequest) {
    if (!ObjectUtils.isEmpty(statusRequest.getSlug()) &&
        !ObjectUtils.isEmpty(statusRequest.getVersion())) {
      CodeCellEntity codeCell = codeCellRepo.findBySlugAndVersion(statusRequest.getSlug(),
          statusRequest.getVersion());
      if (codeCell == null || !codeOwner(codeCell)) {
        throw new RuntimeException("Function not found");
      }
      if (statusRequest.getDeployed() != null && statusRequest.getDeployed()) {
        // Get it from the commit history
        Deployment deployment = commitHistoryRepo.findDeployedCommitByVersionAndSlug(
            statusRequest.getVersion(), statusRequest.getSlug());
        if (deployment == null) {
          throw new RuntimeException("Deployment not found for " +
              statusRequest.getSlug()  + " with version " +
              statusRequest.getVersion());
        }
        return new StatusResponse().isReady(
            !ObjectUtils.isEmpty(deployment.getSchema()) &&
                !ObjectUtils.isEmpty(deployment.getPayload()));
      } else {
        return new StatusResponse().isReady(
            !ObjectUtils.isEmpty(codeCell.getFullOpenApiSchema()) &&
                !ObjectUtils.isEmpty(codeCell.getJsonSchema()));
      }
    }
    throw new RuntimeException("Invalid request");
  }

  private boolean codeOwner(CodeCellEntity codeCell) {
    return codeCell == null || codeCell.getUserId().equals(FHUtils.getSessionUser().getUid());
  }

  private Boolean isBelowActiveLimit(String userId) {
    return codeCellRepo.numActiveCells(userId) < Integer.MAX_VALUE;
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
