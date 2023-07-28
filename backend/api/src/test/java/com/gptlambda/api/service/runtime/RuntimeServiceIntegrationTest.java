package com.gptlambda.api.service.runtime;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptlambda.api.Code;
import com.gptlambda.api.CodeUpdateResponse;
import com.gptlambda.api.data.postgres.entity.CodeCellEntity;
import com.gptlambda.api.data.postgres.entity.CommitHistoryEntity;
import com.gptlambda.api.data.postgres.entity.UserEntity;
import com.gptlambda.api.data.postgres.repo.CodeCellRepo;
import com.gptlambda.api.data.postgres.repo.CommitHistoryRepo;
import com.gptlambda.api.data.postgres.repo.UserRepo;
import com.gptlambda.api.service.ServiceTestConfiguration;
import com.gptlambda.api.service.utils.GPTLambdaUtils;
import com.gptlambda.api.utils.migration.FlywayMigration;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Bizuwork Melesse
 * created on 7/26/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class RuntimeServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CodeCellRepo codeCellRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private CommitHistoryRepo commitHistoryRepo;

    @Autowired
    private FlywayMigration flywayMigration;

    final String rawCode = "import moment from \"npm:moment\";\n"
        + "\n"
        + "interface RequestPayload {\n"
        + "  greeting?: String,\n"
        + "  day?: String\n"
        + "}\n"
        + "\n"
        + "export default async function(payload: RequestPayload) {\n"
        + "  console.log(\"Default function reached in user code\")\n"
        + "  return `${payload.day}: ${payload.greeting}, time: ${moment().format('MMMM Do YYYY, h:mm:ss a')}`\n"
        + "}";

    private UserEntity user;


    @BeforeClass
    public void setup() {
        flywayMigration.migrate(true);
    }

    @AfterTest
    public void teardown() {
    }


    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
        user = new UserEntity();
        user.setUid(GPTLambdaUtils.generateUid(GPTLambdaUtils.SHORT_UID_LENGTH));
        user.setFullName("Test Case");
        userRepo.save(user);
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void updateCodeTest() {
        String encodedCode = Base64.getEncoder().encodeToString(rawCode.getBytes());
        Code code = new Code()
            .code(encodedCode)
            .userId(user.getUid())
            .functionName("theVeryBestFunction");
        CodeUpdateResponse response = runtimeService.updateCode(code);
        assertNotNull(response);
        assertNotNull(response.getUid());

        code = new Code()
            .isActive(true)
            .isPublic(true)
            .uid(response.getUid())
            .description("This is the best function there is!")
            .fieldsToUpdate(List.of("is_active", "is_public", "description"));

        response = runtimeService.updateCode(code);
        assertNotNull(response);
        assertNotNull(response.getUid());

        List<CommitHistoryEntity> commitHistory = commitHistoryRepo
            .findByCodeCellId(UUID.fromString(response.getUid()));
        assertNotNull(commitHistory);
        assertEquals(2, commitHistory.size());

        Code savedCode = runtimeService.getCodeDetail(code.getUid());
        assertNotNull(savedCode);

        // Ensure the decoded code matches the raw code
        String decodedRawCode = runtimeService.getUserCode(code.getUid());
        assertNotNull(decodedRawCode);
        assertEquals(decodedRawCode, rawCode);
    }

    @Test
    public void getUserCodeTest() {
        CodeCellEntity codeCell = new CodeCellEntity();
        codeCell.setUid(UUID.randomUUID());
        codeCell.setCode(rawCode);
        codeCell.setDescription("This is a demo code");
        codeCell.setUserId(user.getUid());
        codeCell.setIsActive(true);
        codeCell.setFunctionName("demo_code");
        codeCell.setSlug("demo-code");
        codeCell.setVersion(runtimeService.generateCodeVersion());
        codeCellRepo.save(codeCell);

        String code = runtimeService.getUserCode(codeCell.getUid().toString());
        assertNotNull(code);
    }

    @Test
    public void generateDtoTest() {
        String payload = "{\n"
            + "  \"companyName\": \"QAAutomation\",\n"
            + "  \"companyEmailId\": \"qaautomation@org.com\",\n"
            + "  \"companyNumber\": \"+353891234121\",\n"
            + "  \"companyAddress\": \"12, HeneryStreet, Dublin, D12PW20\",\n"
            + "  \"supportedSalaryBanks\": [\n"
            + "    \"AIB\",\n"
            + "    \"BOI\",\n"
            + "    \"PSB\"\n"
            + "  ],\n"
            + "  \"booleanValueFalse\": false,\n"
            + "  \"booleanValueTrue\": true,\n"
            + "  \"nullValue\": null,\n"
            + "  \"doubleValue\": 1.22222,\n"
            + "  \"floatValue\": 1.34,\n"
            + "  \"employee\": [\n"
            + "    {\n"
            + "      \"firstName\": \"Vibha\",\n"
            + "      \"lastName\": \"Singh\",\n"
            + "      \"age\": 30,\n"
            + "      \"salary\": 75000,\n"
            + "      \"designation\": \"Manager\",\n"
            + "      \"contactNumber\": \"+919999988822\",\n"
            + "      \"emailId\": \"abc@test.com\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"firstName\": \"Neha\",\n"
            + "      \"lastName\": \"Verma\",\n"
            + "      \"age\": 25,\n"
            + "      \"salary\": 60000,\n"
            + "      \"designation\": \"Lead\",\n"
            + "      \"contactNumber\": \"+914442266221\",\n"
            + "      \"emailId\": \"xyz@test.com\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"firstName\": \"Rajesh\",\n"
            + "      \"lastName\": \"Gupta\",\n"
            + "      \"age\": 20,\n"
            + "      \"salary\": 40000,\n"
            + "      \"designation\": \"Intern\",\n"
            + "      \"contactNumber\": \"+919933384422\",\n"
            + "      \"emailId\": \"pqr@test.com\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"contractors\": [\n"
            + "    {\n"
            + "      \"firstName\": \"John\",\n"
            + "      \"lastName\": \"Mathew\",\n"
            + "      \"contractFrom\": \"Jan-2018\",\n"
            + "      \"contractTo\": \"Aug-2022\",\n"
            + "      \"contactNumber\": \"+919631384422\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"firstName\": \"Seema\",\n"
            + "      \"lastName\": \"Prasad\",\n"
            + "      \"contractFrom\": \"Jun-2019\",\n"
            + "      \"contractTo\": \"Jun-2023\",\n"
            + "      \"contactNumber\": \"+919688881422\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"companyPFDeails\": {\n"
            + "    \"pfName\": \"XYZ\",\n"
            + "    \"pfYear\": 2020,\n"
            + "    \"noOfEmployees\": 100\n"
            + "  }\n"
            + "}";
        String dto = runtimeService.generatePayloadDto(payload);
        assertNotNull(dto);
    }
}
