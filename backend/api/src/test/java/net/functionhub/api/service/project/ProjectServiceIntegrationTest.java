package net.functionhub.api.service.project;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.FHFunction;
import net.functionhub.api.FHFunctions;
import net.functionhub.api.ForkRequest;
import net.functionhub.api.PageableRequest;
import net.functionhub.api.PageableResponse;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.ProjectEntity;
import net.functionhub.api.data.postgres.entity.ProjectItemEntity;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.ProjectItemRepo;
import net.functionhub.api.data.postgres.repo.ProjectRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.service.runtime.RuntimeService;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayPostgresMigration;
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
    private ProjectRepo projectRepo;

    @Autowired
    private FlywayPostgresMigration flywayPostgresMigration;

    @Autowired
    private RuntimeService runtimeService;

    private UserProjection user;

    @BeforeClass
    public void setup() {
    }

    @AfterTest
    public void teardown() {
    }


    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
        flywayPostgresMigration.migrate(true);
        String userId = "u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH);
        testHelper.prepareSecurity(userId);
        userService.getOrCreateUserprofile();
        try {
            Thread.sleep(1000L);
            user = userRepo.findByProjectId(userId);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
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
    public void forkIntoEmptyProjectsTest() {
        FHFunctions functions = createAndAssertFunctions();

        // Create a new project and fork the function to that
        ProjectCreateRequest projRequest = new ProjectCreateRequest()
            .name("My Demo Project")
            .description("This is a demo project created by TestNG");
        Projects projects = projectService.createProject(projRequest);
        assertNotNull(projects);
        assertNotNull(projects.getProjects());
        assertEquals(2, projects.getProjects().size());

        CodeUpdateResult forkResult = projectService.forkCode(new ForkRequest()
            .projectId(null)
            .parentCodeId(functions.getFunctions().get(0).getCodeId()));
        assertNotNull(forkResult);
        assertNotNull(forkResult.getVersion());
        assertNotNull(forkResult.getCodeId());
        assertNotNull(forkResult.getCodeId());

        CodeCellEntity forkedCell = codeCellRepo.findById(forkResult.getCodeId()).orElse(null);
        assertNotNull(forkedCell);
        assertNotNull(forkedCell.getParentId());
        assertEquals(functions.getFunctions().get(0).getCodeId(), forkedCell.getParentId());

        CodeCellEntity parentCell = codeCellRepo
            .findById(functions.getFunctions().get(0).getCodeId()).orElse(null);
        assertNotNull(parentCell);
        assertEquals(1, parentCell.getForkCount().longValue());

        ProjectItemEntity projectItem = projectItemRepo.findByCodeId(forkedCell.getId());
        assertNotNull(projectItem);
        assertEquals(projectItem.getCodeId(), forkedCell.getId());

        ProjectEntity projectEntity = projectRepo.findById(projectItem.getProjectId()).orElse(null);
        assertNotNull(projectEntity);
        assertEquals(projectEntity.getProjectName(), "Untitled");
        assertEquals(projectEntity.getDescription(), "My first project");
    }

    @Test
    public void forkIntoExistingProjectTest() {
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
        assertNotNull(forkResult.getCodeId());
        assertNotNull(forkResult.getCodeId());

        CodeCellEntity forkedCell = codeCellRepo.findById(forkResult.getCodeId()).orElse(null);
        assertNotNull(forkedCell);
        assertNotNull(forkedCell.getParentId());
        assertEquals(functions.getFunctions().get(0).getCodeId(), forkedCell.getParentId());

        CodeCellEntity parentCell = codeCellRepo
            .findById(functions.getFunctions().get(0).getCodeId()).orElse(null);
        assertNotNull(parentCell);
        assertEquals(1, parentCell.getForkCount().longValue());

        ProjectItemEntity projectItem = projectItemRepo.findByCodeId(forkedCell.getId());
        assertNotNull(projectItem);
        assertEquals(projectItem.getProjectId(), projectId);
        assertEquals(projectItem.getCodeId(), forkedCell.getId());
    }

    @Test
    public void updateFunctionTest() {
        FHFunctions functions = createAndAssertFunctions();
        FHFunction function = functions.getFunctions().get(0);
        ProjectItemEntity projectItem = projectItemRepo.findByCodeId(function.getCodeId());
        FHFunction functionToUpdate = new FHFunction().tags("apple, google, facebook")
            .projectId(projectItem.getProjectId())
            .codeId(function.getCodeId());
        functions = projectService.updateFunction(functionToUpdate);
        assertNotNull(functions);
        assertEquals(1, functions.getFunctions().size());
        FHFunction result = functions.getFunctions().get(0);
        assertEquals(functionToUpdate.getTags(), result.getTags());
        assertTrue(result.getUpdatedAt() >= result.getCreatedAt());
    }

    @Test
    public void getAllPublicFunctionsTest() {
        int numFunctions = 12;
        int numPublic = 6;
        int numProjects = 3;
        createMultipleFunctions(numFunctions, numPublic, numProjects, 0);

        int totalPublicFunctions = numProjects * numPublic;
        PageableResponse response = projectService.getAllPublicFunctions(new PageableRequest().pageNum(0)
            .limit(Integer.MAX_VALUE));
        assertNotNull(response);
        assertEquals(1, (int) response.getNumPages());
        assertEquals(totalPublicFunctions, (long) response.getTotalRecords());
        assertEquals(totalPublicFunctions, response.getRecords().size());
        assertFHFunctions(response.getRecords());

        response = projectService.getAllPublicFunctions(new PageableRequest()
            .pageNum(0)
            .limit(2));
        assertNotNull(response);
        assertEquals(9, (int) response.getNumPages());
        assertEquals(totalPublicFunctions, (long) response.getTotalRecords());
        assertEquals(2, response.getRecords().size());
        assertFHFunctions(response.getRecords());
    }

    @Test
    public void searchAllPublicFunctionsTest() {
        int numFunctions = 12;
        int numPublic = 6;
        int numProjects = 3;
        int numWithTags = 4;
        createMultipleFunctions(numFunctions, numPublic, numProjects, numWithTags);

        PageableResponse response = projectService.getAllPublicFunctions(new PageableRequest()
            .pageNum(0)
            .query("facebook")
            .limit(Integer.MAX_VALUE));
        assertNotNull(response);
        int totalTaggedFunctions = numWithTags * numProjects;
        assertEquals(1, (int) response.getNumPages());
        assertEquals(totalTaggedFunctions, (long) response.getTotalRecords());
        assertEquals(totalTaggedFunctions, response.getRecords().size());

        response = projectService.getAllPublicFunctions(new PageableRequest()
            .pageNum(0)
            .query("apple")
            .limit(3));
        assertNotNull(response);
        assertEquals(4, (int) response.getNumPages());
        assertEquals(totalTaggedFunctions, (long) response.getTotalRecords());
        assertEquals(3, response.getRecords().size());

        response = projectService.getAllPublicFunctions(new PageableRequest()
            .pageNum(4)
            .query("custom_greeter")
            .limit(4));
        assertNotNull(response);
        assertEquals(5, (int) response.getNumPages());
        assertEquals(18, (long) response.getTotalRecords());
        assertEquals(2, response.getRecords().size());
        assertFHFunctions(response.getRecords());

        response = projectService.getAllPublicFunctions(new PageableRequest()
            .pageNum(4)
            .query("CUSTOM_GREETER")
            .limit(4));
        assertNotNull(response);
        assertEquals(5, (int) response.getNumPages());
        assertEquals(18, (long) response.getTotalRecords());
        assertEquals(2, response.getRecords().size());
        assertFHFunctions(response.getRecords());
    }

    private void assertFHFunctions(List<FHFunction> records) {
        assertTrue(records.size() > 0);
        for (FHFunction record : records) {
            assertNotNull(record.getOwnerId());
//            assertNotNull(record.getOwnerUsername());
//            assertNotNull(record.getOwnerAvatar());
//            assertTrue(record.getOwnerAvatar().startsWith("https"));
            assertNotNull(record.getOwnerId());
            assertNotNull(record.getSlug());
            assertNotNull(record.getName());
            assertNotNull(record.getIsPublic());
            assertNotNull(record.getCreatedAt());
            assertNotNull(record.getUpdatedAt());
        }
    }

    private void createMultipleFunctions(int numFunctions, int numPublic, int numProjects, int numWithTags) {
        for (int i = 0; i < numProjects; i++) {
            ProjectCreateRequest request1 = new ProjectCreateRequest()
                .name("My Demo Project " + i)
                .description("This is a demo project created by TestNG " + 1);
            Projects projects = projectService.createProject(request1);
            String projectId = projects.getProjects().get(0).getProjectId();
            int numPrivateFunctions = numFunctions - numPublic;
            for (int j = 0; j < numPublic; j++) {
                projectService.createFunction(projectId);
            }
            int tagged = numWithTags;
            // Toggle all these functions to public
            for (FHFunction function : projectService.getAllFunctions(projectId).getFunctions()) {
                Code code = new Code().isPublic(true).codeId(function.getCodeId())
                    .fieldsToUpdate(List.of("is_public"));
                runtimeService.updateCode(code, false);
                if (tagged > 0) {
                    projectService.updateFunction(
                        new FHFunction().tags("apple, google, facebook")
                            .projectId(projectId)
                            .codeId(function.getCodeId()));
                    tagged--;
                }
            }

            // Create private functions
            for (int j = 0; j < numPrivateFunctions; j++) {
                projectService.createFunction(projectId);
            }
        }

        long totalFunctionCount = (long) numFunctions * numProjects;
        assertEquals(numProjects, projectRepo.count());
        assertEquals(totalFunctionCount, projectItemRepo.count());
        assertEquals(totalFunctionCount, codeCellRepo.count());
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
        assertNotNull(result.getCodeId());
        assertNotNull(result.getVersion());

        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
        assertNotNull(functions);
        assertEquals(1, functions.getFunctions().size());
        assertNotNull(functions.getFunctions().get(0).getOwnerId());
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
        assertEquals(0, projectItemRepo.findByProjectIdOrderByCreatedAtDesc(projects.getProjects().get(0).getProjectId()).size());
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
        assertNotNull(result.getCodeId());
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
        assertEquals(0, projectItemRepo.findByProjectIdOrderByCreatedAtDesc(projects.getProjects().get(0).getProjectId()).size());
    }
}
