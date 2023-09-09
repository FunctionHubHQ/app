package net.functionhub.api.service.runtime;


import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.Projects;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.CommitHistoryEntity;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.CommitHistoryRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.service.project.ProjectService;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayPostgresMigration;
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
    private UserService userService;

    @Autowired
    private ServiceTestHelper testHelper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private CommitHistoryRepo commitHistoryRepo;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FlywayPostgresMigration flywayPostgresMigration;

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
        flywayPostgresMigration.migrate(true);
        String userId = "u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH);
        testHelper.prepareSecurity(userId);
        userService.getOrCreateUserprofile();
        try {
            Thread.sleep(5000L);
            user = userRepo.findById(userId).orElse(null);
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
    public void updateCodeTest() {
        ProjectCreateRequest request = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        Projects projects = projectService.createProject(request);

        String encodedCode = Base64.getEncoder().encodeToString(rawCode.getBytes());
        Code code = new Code()
            .code(encodedCode)
            .projectId(projects.getProjects().get(0).getProjectId());
        CodeUpdateResult response = runtimeService.updateCode(code, false
        );
        assertNotNull(response);
        assertNotNull(response.getCodeId());

        code = new Code()
            .isActive(true)
            .isPublic(true)
            .codeId(response.getCodeId())
            .fieldsToUpdate(List.of("is_active", "is_public"));

        response = runtimeService.updateCode(code, false);
        assertNotNull(response);
        assertNotNull(response.getCodeId());
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        List<CommitHistoryEntity> commitHistory = commitHistoryRepo
            .findByCodeCellId(response.getCodeId());
        assertNotNull(commitHistory);
        assertEquals(2, commitHistory.size());

        Code savedCode = runtimeService.getCodeDetail(code.getCodeId(), false);
        assertNotNull(savedCode);

        // Ensure the decoded code matches the raw code
        String decodedRawCode = runtimeService.getUserCode(code.getCodeId());
        String template = testHelper.loadFile("classpath:ts/workerTemplate.ts");
        assertNotNull(decodedRawCode);
        assertNotNull(template);
        assertTrue(decodedRawCode.contains("b7dbbfbb7723221173444ae44b734f5b5cbaa465b9cfa2a70c9819394acc1291"));
        assertNotNull(decodedRawCode.concat("FUNCTION_HUB_KEY"));
        assertTrue(decodedRawCode.contains(rawCode));
    }

    @Test
    public void getUserCodeTest() {
        CodeCellEntity codeCell = new CodeCellEntity();
        codeCell.setId(FHUtils.generateEntityId("cc"));
        codeCell.setCode(Base64.getEncoder().encodeToString(rawCode.getBytes()));
        codeCell.setDescription("This is a demo code");
        codeCell.setUserId(user.getId());
        codeCell.setIsActive(true);
        codeCell.setFunctionName("demo_code");
        codeCell.setSlug("demo-code");
        codeCell.setVersion(runtimeService.generateCodeVersion());
        codeCellRepo.save(codeCell);

        String code = runtimeService.getUserCode(codeCell.getId());
        assertNotNull(code);
    }

    @Test(enabled = false)
    public void generateOpenApiSpecTest() {
        String tsSpec = "export interface Author = {\n"
            + "  name: string;\n"
            + "  image: string;\n"
            + "  designation: string;\n"
            + "};\n"
            + "\n"
            + "export interface Blog = {\n"
            + "  id: number;\n"
            + "  title: string;\n"
            + "  paragraph: string;\n"
            + "  image: string;\n"
            + "  author: Author;\n"
            + "  tags: string[];\n"
            + "  publishDate: string;\n"
            + "};\n";
        runtimeService.generateJsonSchema(FHUtils.getSessionUser(), Base64.getEncoder().encodeToString(tsSpec.getBytes()),
            UUID.randomUUID().toString());
    }
}
