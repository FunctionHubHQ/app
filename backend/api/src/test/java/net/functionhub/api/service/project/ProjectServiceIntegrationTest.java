package net.functionhub.api.service.project;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import java.lang.reflect.Method;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.FHFunctions;
import net.functionhub.api.ForkRequest;
import net.functionhub.api.Project;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.ProjectItemRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Bizuwork Melesse
 * created on 7/26/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class ProjectServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceTestHelper testHelper;

    @Autowired
    private CodeCellRepo codeCellRepo;

    @Autowired
    private ProjectItemRepo projectItemRepo;

    @Autowired
    private FlywayMigration flywayMigration;

    private UserProjection user;

    @BeforeClass
    public void setup() {
        flywayMigration.migrate(true);
        String userId = "u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH);
        testHelper.prepareSecurity(userId);
        userService.getOrCreateUserprofile();
        try {
            Thread.sleep(5000L);
            user = userRepo.findProjectionByUid(userId);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @AfterTest
    public void teardown() {
    }


    @BeforeMethod
    public void beforeEachTest(Method method) {
        flywayMigration.migrate(true);
        log.info("  Testcase: " + method.getName() + " has started");
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void createProjectTest() {
        ProjectCreateRequest request1 = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        ProjectCreateRequest request2 = new ProjectCreateRequest()
            .name("My Demo Project 2")
            .description("This is a demo project created by TestNG 2");
        Projects projects = projectService.createProject(request1);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(1, projects.getProjects().size());

        projects = projectService.createProject(request2);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(2, projects.getProjects().size());
        assertNotNull(projects.getProjects().get(0).getProjectId());
        assertEquals(request2.getName(), projects.getProjects().get(0).getName());
        assertEquals(request2.getDescription(), projects.getProjects().get(0).getDescription());
    }

    @Test
    public void getAllProjectsTest() {
        ProjectCreateRequest request1 = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        ProjectCreateRequest request2 = new ProjectCreateRequest()
            .name("My Demo Project 2")
            .description("This is a demo project created by TestNG 2");
        Projects projects = projectService.createProject(request1);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(1, projects.getProjects().size());

        projects = projectService.createProject(request2);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(2, projects.getProjects().size());

        projects = projectService.getAllProjects();
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(2, projects.getProjects().size());
    }

    @Test
    public void updateProjectTest() {
        ProjectCreateRequest request1 = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        Projects projects = projectService.createProject(request1);
        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest()
            .projectId(projects.getProjects().get(0).getProjectId())
            .name("Updated name")
            .description("Updated description");
        Projects updatedProjects = projectService.updateProject(updateRequest);
        assertNotNull(updatedProjects);
        assertEquals(updatedProjects.getProjects().get(0).getName(), updateRequest.getName());
        assertEquals(updatedProjects.getProjects().get(0).getDescription(), updateRequest.getDescription());
        assertEquals(updatedProjects.getProjects().get(0).getProjectId(), updateRequest.getProjectId());
    }


    @Test
    public void createFunctionTest() {
        createAndAssertFunctions();
    }

    @Test
    public void forkTest() {
        FHFunctions functions = createAndAssertFunctions();

        // Create a new project and fork the function to that
        ProjectCreateRequest projRequest = new ProjectCreateRequest()
            .name("My Demo Project")
            .description("This is a demo project created by TestNG");
        Projects projects = projectService.createProject(projRequest);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(2, projects.getProjects().size());
        String projectId = projects.getProjects().get(0).getProjectId();

        CodeUpdateResult forkResult = projectService.forkCode(new ForkRequest()
            .projectId(projectId)
            .parentCodeId(functions.getFunctions().get(0).getCodeId()));
        assertNotNull(forkResult);
        assertNotNull(forkResult.getVersion());
        assertNotNull(forkResult.getUid());
        assertNotNull(forkResult.getUid());

        CodeCellEntity forkedCell = codeCellRepo.findByUid(UUID.fromString(forkResult.getUid()));
        assertNotNull(forkedCell);
        assertNotNull(forkedCell.getParentId());
        assertEquals(functions.getFunctions().get(0).getCodeId(), forkedCell.getParentId().toString());
    }

    private FHFunctions createAndAssertFunctions() {
        ProjectCreateRequest request1 = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        Projects projects = projectService.createProject(request1);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(1, projects.getProjects().size());

        CodeUpdateResult result = projectService.createFunction(projects.getProjects().get(0).getProjectId());
        assertNotNull(result);
        assertNotNull(result.getSlug());
        assertNotNull(result.getUid());
        assertNotNull(result.getVersion());

        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
        assertNotNull(functions);
        assertEquals(1, functions.getFunctions().size());
        assertNotNull(functions.getFunctions().get(0).getSlug());
        assertNotNull(functions.getFunctions().get(0).getName());
        assertNotNull(functions.getFunctions().get(0).getIsPublic());
        assertNotNull(functions.getFunctions().get(0).getCreatedAt());
        assertNotNull(functions.getFunctions().get(0).getUpdatedAt());
        return functions;
    }

    @Test
    public void deleteFunctionTest() {
        ProjectCreateRequest request1 = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        Projects projects = projectService.createProject(request1);
        CodeUpdateResult result = projectService.createFunction(projects.getProjects().get(0).getProjectId());
        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
        assertNotNull(functions);
        assertEquals(1, functions.getFunctions().size());
        functions = projectService.deleteFunction(functions.getFunctions().get(0).getSlug());
        assertNotNull(functions);
        assertEquals(0, functions.getFunctions().size());
        assertNull(codeCellRepo.findBySlug(result.getSlug()));
        assertEquals(0, projectItemRepo.findByProjectIdOrderByCreatedAtDesc(UUID.fromString(projects.getProjects().get(0).getProjectId())).size());
    }

    @Test
    public void deleteProjectTest() {
        ProjectCreateRequest request1 = new ProjectCreateRequest()
            .name("My Demo Project 1")
            .description("This is a demo project created by TestNG 1");
        Projects projects = projectService.createProject(request1);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(1, projects.getProjects().size());

        ProjectCreateRequest request2 = new ProjectCreateRequest()
            .name("My Demo Project 2")
            .description("This is a demo project created by TestNG 2");
        projects = projectService.createProject(request2);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(2, projects.getProjects().size());

        CodeUpdateResult result = projectService.createFunction(projects.getProjects().get(0).getProjectId());
        assertNotNull(result);
        assertNotNull(result.getSlug());
        assertNotNull(result.getUid());
        assertNotNull(result.getVersion());

        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
        assertNotNull(functions);
        assertEquals(1, functions.getFunctions().size());

        Projects remainingProjects = projectService.deleteProject(projects.getProjects().get(0).getProjectId());
        assertNotNull(remainingProjects);
        assertEquals(1, remainingProjects.getProjects().size());
        assertEquals(projects.getProjects().get(1).getProjectId(),
            remainingProjects.getProjects().get(0).getProjectId());

        assertNull(codeCellRepo.findBySlug(result.getSlug()));
        assertEquals(0, projectItemRepo.findByProjectIdOrderByCreatedAtDesc(UUID.fromString(projects.getProjects().get(0).getProjectId())).size());
    }
}
