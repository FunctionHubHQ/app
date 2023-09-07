package net.functionhub.api.controller;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.GPTCompletionRequest;
import net.functionhub.api.GPTMessage;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.data.postgres.entity.ApiKeyEntity;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.EntitlementEntity;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.data.postgres.repo.ApiKeyRepo;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.dto.SessionUser;
import net.functionhub.api.props.MessagesProps;
import net.functionhub.api.service.runtime.RuntimeService;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayPostgresMigration;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Slf4j
@Transactional
@SpringBootTest(classes = ControllerTestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RuntimeControllerIntegrationTest extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port = 9090;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CodeCellRepo codeCellRepo;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private FlywayPostgresMigration flywayPostgresMigration;

    @Autowired
    private ServiceTestHelper testHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ApiKeyRepo apiKeyRepo;

    @Autowired
    private EntitlementRepo entitlementRepo;

    @Autowired
    private MessagesProps messagesProps;

    @Autowired
    private RuntimeService runtimeService;

    private final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};;

    private SessionUser sessionUser;

    @BeforeClass
    public void setup() {
        flywayPostgresMigration.migrate(true);
        String userId = "u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH);
        testHelper.prepareSecurity(userId);
        userService.getOrCreateUserprofile();
        try {
            sessionUser = new SessionUser();
            Thread.sleep(2000L);
            UserProjection userProjection = userRepo.findProjectionByUid(userId);
            sessionUser.setName(userProjection.getName());
            sessionUser.setUid(userProjection.getUid());
            sessionUser.setEmail(userProjection.getEmail());
            sessionUser.setUsername(userProjection.getUsername());
            ApiKeyEntity apiKeyEntity = apiKeyRepo.findOldestApiKey(userProjection.getUid());
            if (apiKeyEntity != null) {
                // userEntity could be null if this is the registration flow
                sessionUser.setApiKey(apiKeyEntity.getApiKey());
            }
            testHelper.setSessionUser(sessionUser);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @AfterTest
    public void teardown() {
    }


    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void stressTestNonBlockingCalls() throws JsonProcessingException {
        final CodeUpdateResult nonBlockingCodeCell = createCodeCell(nonBlockingCode);

        // We are submitting 250 concurrent requests at the same millisecond. That's
        // extremely unlikely even in the worst possible case for our use case.
        int numRequests = 250; // Can't go beyond this due to Tomcat default limits. Should
        // consider using Tomcat's NIO in the future.
        // Average time per execution: 30ms

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Map<String, Object>>> tasks = new ArrayList<>();
        for (int i = 0; i < numRequests; i++) {
            tasks.add(
                executorService.submit(() -> runDevAndGetResult(nonBlockingCodeCell)));
        }
        long start = System.currentTimeMillis();
        List<Map<String, Object>>  results = tasks.stream()
            .map(it -> {
                try {
                    return it.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.info("Total time to process {} requests: {} ms. Average time per request: {}", numRequests, totalTime, totalTime/(numRequests * 1.0));
        assertNotNull(results);
        assertEquals(numRequests, results.size());
        for (Map<String, Object> result : results) {
            assertNotNull(result.get("greeting"));
        }
    }

    @Test(enabled = false) // The behavior of blocking + non-blocking tests is \
    // unpredictable so should be enabled for local testing only
    public void stressTestBlockingCalls() throws JsonProcessingException {
        // Stress test concurrency and applying cpu and memory limits correctly
        final CodeUpdateResult nonBlockingCodeCell = createCodeCell(nonBlockingCode);
        final CodeUpdateResult blockingCodeCell = createCodeCell(blockingCode);

        // We are submitting 250 concurrent requests at the same millisecond. That's
        // extremely unlikely even in the worst possible case for our use case.
        int numNonBlockingRequests = 20;
        int numBlockingRequests = 20;
        log.info("Non-blocking: {}", nonBlockingCodeCell.getUid());
        log.info("Blocking: {}", blockingCodeCell.getUid());

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Map<String, Object>>> tasks = new ArrayList<>();

        for (int i = 0; i < numNonBlockingRequests; i++) {
            tasks.add(
                executorService.submit(() -> runDevAndGetResult(nonBlockingCodeCell)));
        }

        for (int i = 0; i < numBlockingRequests; i++) {
            tasks.add(
                executorService.submit(() -> runDevAndGetResult(blockingCodeCell)));
        }

        Collections.shuffle(tasks);

        long start = System.currentTimeMillis();
        List<Map<String, Object>>  results = tasks.stream()
            .map(it -> {
                try {
                    return it.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
        long end = System.currentTimeMillis();
        long totalTime = end - start;
        int totalRequest = numBlockingRequests + numNonBlockingRequests;
        log.info("Total time to process {} requests: {} ms. Average time per request: {}", totalRequest, totalTime, totalTime/(totalRequest * 1.0));

        int blockingResults = 0;
        int nonBlockingResults = 0;

        assertEquals(totalRequest, results.size());
        for (Map<String, Object> result : results) {
            Object error = result.get("error");
            Object greeting = result.get("greeting");
            if (error != null) {
                if ( error.toString().contains("Out of memory") ||
                    error.toString().contains("CPU timeout")) {
                    blockingResults++;
                } else {
                    int x = 1;
                }
            } else if (greeting != null && greeting.toString().startsWith("Hello, world!")) {
                nonBlockingResults++;
            }
        }
        log.info("Num blocking results: {}", blockingResults);
        log.info("Num non-blocking results: {}", nonBlockingResults);
        assertEquals(numBlockingRequests, blockingResults);
        assertEquals(numNonBlockingRequests, nonBlockingResults);
    }

    @Test(enabled = false) // Unstable
    public void invocationLimitTest() throws JsonProcessingException  {
        EntitlementEntity entity = entitlementRepo.findByUserId(sessionUser.getUid());
        entity.setMaxInvocations(2L);
        entitlementRepo.save(entity);
        CodeUpdateResult updateResult = createCodeCell(code);

        // No limits on dev
        for (int i = 0; i < 10; i++) {
            runDevCodeCell(updateResult);
        }

        // Deploy and attempt to make more requests than allowed
        deployCodeCell(updateResult);
        for (int i = 0; i < 12; i++) {
            Map<String, Object> execResult = runProdCodeCell(updateResult);
            assertNotNull(execResult);
            if (i < 10) {
                assertNotNull(execResult.get("temperature"));
            } else {
                // expect an error on the third request
                String error = execResult.get("error").toString();
                assertNotNull(error);
                assertTrue(error.startsWith(messagesProps.getInvocationLimitReached()));
            }
        }
    }

    @Test
    public void fullFlowTest() throws JsonProcessingException {
        // 1. Create code cell
        CodeUpdateResult updateResult = createCodeCell(code);

        // 2. Run it
        runDevCodeCell(updateResult);

        // 4. Deploy it
        deployCodeCell(updateResult);

        // 5. Make dev GPT function call
        GPTCompletionRequest devCompletionRequest = new GPTCompletionRequest();
        GPTMessage message = new GPTMessage()
            .role("user")
            .content("What is the current time and weather in Boston in degrees Celsius?");
        devCompletionRequest.setMessages(List.of(message));

        String completionResponseDevResponseStr = request("/completion/" + updateResult.getSlug(),
            "POST", devCompletionRequest);

        Map<String, Object> completionResponseDevResponse = objectMapper
            .readValue(completionResponseDevResponseStr, typeRef);
        assertNotNull(completionResponseDevResponse);
        assertTrue(completionResponseDevResponse.get("choices").toString().contains("Boston"));
        assertTrue(completionResponseDevResponse.get("content").toString().contains("rainy"));


        // 6. Make prod GPT function call
        GPTCompletionRequest prodCompletionRequest = new GPTCompletionRequest();
        message = new GPTMessage()
            .role("user")
            .content("What is the current time and weather in Chicago?");
        devCompletionRequest.setMessages(List.of(message));

        String completionResponseProdResponseStr = request("/completion",
            "POST", prodCompletionRequest);
// TODO: prod requests require user's own OpenAI key
        assertNotNull(completionResponseProdResponseStr);
        Map<String, Object> deployedCompletionResponse = objectMapper.readValue(
            completionResponseProdResponseStr, typeRef);
        assertNotNull(deployedCompletionResponse);
        assertTrue(deployedCompletionResponse.get("content").toString().contains("Chicago"));
        assertTrue(deployedCompletionResponse.get("content").toString().contains("rainy"));
    }

    private CodeUpdateResult createCodeCell(String rawCode) throws JsonProcessingException {
        String codeEncoded = Base64.getEncoder().encodeToString(rawCode.getBytes());

        String updateResultStr = request("/update-code", "POST", new Code()
            .code(codeEncoded));

        CodeUpdateResult updateResult = objectMapper
            .readValue(updateResultStr, CodeUpdateResult.class);
        assertNotNull(updateResult.getUid());
        return updateResult;
    }

    private void runDevCodeCell(CodeUpdateResult updateResult) throws JsonProcessingException {
        String city = "Chicago, IL";
        Map<String, Object> payload = new HashMap<>();
        payload.put("location", city);
        String execResultStr = request("/d/" + updateResult.getSlug(), "POST",
            new Gson().toJson(payload));

        Map<String, Object> execResult = objectMapper
            .readValue(execResultStr, typeRef);
        assertNotNull(execResult);
        assertNotNull(execResult.get("temperature"));
    }

    private Map<String, Object> runDevAndGetResult(CodeUpdateResult updateResult)
        throws JsonProcessingException, InterruptedException {
        Thread.sleep(30);
        String city = "Chicago, IL";
        Map<String, Object> payload = new HashMap<>();
        payload.put("location", city);
        String execResultStr = request("/d/" + updateResult.getSlug(), "POST",
            new Gson().toJson(payload));
        return objectMapper
            .readValue(execResultStr, typeRef);
    }

    private Map<String, Object> runProdCodeCell(CodeUpdateResult updateResult) throws JsonProcessingException {
        String city = "Chicago, IL";
        Map<String, Object> payload = new HashMap<>();
        payload.put("location", city);
        String execResultStr = request("/" + updateResult.getSlug(), "POST",
            new Gson().toJson(payload));
       return objectMapper
            .readValue(execResultStr, typeRef);
    }

    private void deployCodeCell(CodeUpdateResult updateResult) throws JsonProcessingException {
        String deployResponseStr = request("/deploy",
            "POST", new ExecRequest()
                .uid(updateResult.getUid()));

        GenericResponse deployResponse = objectMapper.readValue(deployResponseStr, GenericResponse.class);
        assertNotNull(deployResponse.getStatus());

        CodeCellEntity codeCell = codeCellRepo.findByUid(updateResult.getUid());
        assertTrue(codeCell.getDeployed());
        String schema = runtimeService.getJsonSchema(updateResult.getUid());
        assertNotNull(schema);
    }

    private String request(String path, String httpMethod, Object payload) {
        String host = "localhost";
        int _port = port;
        String fullUrl = String.format("http://%s:%s%s", host, _port, path);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + sessionUser.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (Objects.equals(httpMethod, "POST")) {
            HttpEntity<Object> request = new HttpEntity<>(payload, headers);
            return testRestTemplate.postForObject(fullUrl, request, String.class);
        }
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(fullUrl, HttpMethod.GET, requestEntity, String.class)
            .getBody();
    }

    private final String code = "import moment from \"npm:moment\";\n"
        + "\n"
        + "type TempUnit = \"CELCIUS\" | \"FAHRENHEIT\";\n"
        + "\n"
        + "export interface Request {\n"
        + "  /**\n"
        + "   * The city and state, e.g. San Francisco, CA\n"
        + "   */\n"
        + "  location: string,\n"
        + "\n"
        + "  /**\n"
        + "   * Unit of temperature\n"
        + "   */\n"
        + "  unit?: TempUnit\n"
        + "}\n"
        + "\n"
        + "export interface Response {\n"
        + "    location?: string,\n"
        + "    temperature?: string,\n"
        + "    unit?: TempUnit,\n"
        + "    forecast: string[],\n"
        + "    current_time: string\n"
        + "}\n"
        + "\n"
        + "async function getForecast(): string[] {\n"
        + "  return [\"rainy\", \"windy\"];\n"
        + "}\n"
        + "\n"
        + "/**\n"
        + " * @name get_city_time_and_weather\n"
        + " * @summary Get the current time and weather of any given city. The city must\n"
        + " *              be valid for us to be able to parse and lookup. Temperature unit is\n"
        + " *              optional and will default to celcius.\n"
        + " * @return A valid time and temp\n"
        + " */\n"
        + "export async function handler(request: Request): Promise<Response> {\n"
        + "  console.log(\"Inside child worker\")\n"
        + "  return {\n"
        + "  location: request.location,\n"
        + "   temperature: \"35\",\n"
        + "   unit: request.unit,\n"
        + "   forecast: await getForecast(),\n"
        + "   current_time: moment().format('MMMM Do YYYY, h:mm:ss a')\n"
        + "  };\n"
        + "}";

    private final String nonBlockingCode = "export interface Request {\n"
        + "}\n"
        + "\n"
        + "export interface Response {\n"
        + "}\n"
        + "\n"
        + "\n"
        + "/**\n"
        + " * @name custom_greeter\n"
        + " * @summary A brief summary of what my function does\n"
        + " * @description An extended descripiton of what my function does. This is shown\n"
        + " *              to others if you make the function public.\n"
        + " */\n"
        + "export async function handler(request: Request): Promise<Response> {\n"
        + "  return {\n"
        + "    greeting: `Hello, world!`\n"
        + "  }\n"
        + "}";

    private final String blockingCode = "export interface Request {\n"
        + "}\n"
        + "\n"
        + "export interface Response {\n"
        + "}\n"
        + "\n"
        + "\n"
        + "/**\n"
        + " * @name custom_greeter\n"
        + " * @summary A brief summary of what my function does\n"
        + " * @description An extended descripiton of what my function does. This is shown\n"
        + " *              to others if you make the function public.\n"
        + " */\n"
        + "export async function handler(request: Request): Promise<Response> {\n"
        + "  while (true) {}\n"
        + "  return {\n"
        + "    greeting: `Hello, world!`\n"
        + "  }\n"
        + "}";
}

