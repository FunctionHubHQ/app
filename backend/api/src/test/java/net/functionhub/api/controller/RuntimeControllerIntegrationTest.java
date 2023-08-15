package net.functionhub.api.controller;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.ExecRequest;
import net.functionhub.api.ExecResultAsync;
import net.functionhub.api.FHCompletionRequest;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.service.runtime.RuntimeService;
import net.functionhub.api.service.token.TokenService;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayMigration;
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
    private int port = 8080;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CodeCellRepo codeCellRepo;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private FlywayMigration flywayMigration;

    @Autowired
    private ServiceTestHelper testHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RuntimeService runtimeService;

    private final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};;

    private UserEntity user;
    private final String code = "import moment from \"npm:moment\";\n"
        + "\n"
        + "type TempUnit = \"CELCIUS\" | \"FAHRENHEIT\";\n"
        + "\n"
        + "export interface RequestEntity {\n"
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
        + "export interface ResponseEntity {\n"
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
        + "export async function handler(request: RequestEntity): Promise<ResponseEntity> {\n"
        + "  console.log(\"Inside child worker\")\n"
        + "  return {\n"
        + "  location: request.location,\n"
        + "   temperature: \"35\",\n"
        + "   unit: request.unit,\n"
        + "   forecast: await getForecast(),\n"
        + "   current_time: moment().format('MMMM Do YYYY, h:mm:ss a')\n"
        + "  };\n"
        + "}";


    @BeforeClass
    public void setup() {
//        flywayMigration.migrate(true);
        String userId = "u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH);
        testHelper.prepareSecurity(userId);
        userService.getOrCreateUserprofile();
        String authToken = tokenService.generateJwtToken();
        try {
            Thread.sleep(5000L);
            user = userRepo.findByUid(userId);
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
    public void fullFlowTest() throws InterruptedException, JsonProcessingException {
        String codeEncoded = Base64.getEncoder().encodeToString(code.getBytes());

        // 1. Create code cell
        String updateResultStr = request("/update-code", "POST", new Code()
            .code(codeEncoded)
            .userId(user.getUid()));

        CodeUpdateResult updateResult = objectMapper
            .readValue(updateResultStr, CodeUpdateResult.class);
        assertNotNull(updateResult.getUid());

        // 2. Run it
        String city = "Chicago, IL";
        Map<String, Object> payload = new HashMap<>();
        payload.put("location", city);
        ExecRequest execRequest =  new ExecRequest()
            .uid(updateResult.getUid())
            .execId(UUID.randomUUID().toString())
            .validate(true)
            .payload(new Gson().toJson(payload));
        request("/run", "POST", execRequest);

        // 3. Fetch the result
        Thread.sleep(5000L);
        String execResultStr = request("/e-result?exec_id=" + execRequest.getExecId(),
            "GET", new ExecRequest()
            .uid(updateResult.getUid())
            .payload(new Gson().toJson(payload)));

        ExecResultAsync execResult = objectMapper.readValue(execResultStr, ExecResultAsync.class);
        assertNotNull(execResult);
        assertTrue(execResult.getStdOutStr().contains("child"));

        // 4. Deploy it
        String deployResponseStr = request("/deploy",
            "POST", new ExecRequest()
                .uid(updateResult.getUid()));

        GenericResponse deployResponse = objectMapper.readValue(deployResponseStr, GenericResponse.class);
        assertNotNull(deployResponse.getStatus());

        Thread.sleep(5000L);
        CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(updateResult.getUid()));
        assertTrue(codeCell.getDeployed());
        String schema = runtimeService.getJsonSchema(updateResult.getUid());
        assertNotNull(schema);

        // 5. Make test GPT function call
        FHCompletionRequest devCompletionRequest = new FHCompletionRequest();
        devCompletionRequest.setPrompt("What is the current time and weather in Boston in degrees celcius?");

        String completionResponseDevResponseStr = request("/gpt-completion/" + updateResult.getSlug(),
            "POST", devCompletionRequest);

        Map<String, Object> completionResponseDevResponse = objectMapper
            .readValue(completionResponseDevResponseStr, typeRef);
        assertNotNull(completionResponseDevResponse);
        assertTrue(completionResponseDevResponse.get("content").toString().contains("Boston"));
        assertTrue(completionResponseDevResponse.get("content").toString().contains("rainy"));


        // 6. Make deployed GPT function call
        FHCompletionRequest prodCompletionRequest = new FHCompletionRequest();
        prodCompletionRequest.setPrompt("What is the current time and weather in Chicago?");

        String completionResponseProdResponseStr = request("/gpt-completion/",
            "POST", prodCompletionRequest);

        assertNotNull(completionResponseProdResponseStr);
        Map<String, Object> deployedCompletionResponse = objectMapper.readValue(
            completionResponseProdResponseStr, typeRef);
        assertNotNull(deployedCompletionResponse);
        assertTrue(deployedCompletionResponse.get("content").toString().contains("Chicago"));
        assertTrue(deployedCompletionResponse.get("content").toString().contains("rainy"));
    }

    private String request(String path, String httpMethod, Object payload) {
        String host = "localhost";
        int _port = port + 1;
        String fullUrl = String.format("http://%s:%s%s", host, _port, path);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + user.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (Objects.equals(httpMethod, "POST")) {
            HttpEntity<Object> request = new HttpEntity<>(payload, headers);
            return testRestTemplate.postForObject(fullUrl, request, String.class);
        }
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(fullUrl, HttpMethod.GET, requestEntity, String.class)
            .getBody();
    }
}

