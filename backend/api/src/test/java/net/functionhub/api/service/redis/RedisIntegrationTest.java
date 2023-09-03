package net.functionhub.api.service.redis;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.service.utils.FHUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
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
public class RedisIntegrationTest extends AbstractTestNGSpringContextTests {

    @BeforeClass
    public void setup() {
    }

    @AfterTest
    public void teardown() {
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVITY_KEY_PREFIX = "user_activity:";

    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
//        flywayH2Migration.migrate();
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    public void recordUserActivity(String userId) {
        String key = ACTIVITY_KEY_PREFIX + userId;
        long timestamp = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, "activity", timestamp);
    }

    public long getActivityCountForUserInLastMinutes(String userId, long minutes) {
        String key = ACTIVITY_KEY_PREFIX + userId;
        long currentTime = System.currentTimeMillis();
        long timeThreshold = currentTime - TimeUnit.MINUTES.toMillis(minutes);
        return redisTemplate.opsForZSet().count(key, timeThreshold, currentTime);
    }

    @Test
    public void createInvocationRecordTest() {
        String userId = FHUtils.generateUid(16);
        recordUserActivity(userId);
        long count = getActivityCountForUserInLastMinutes(userId, 1L);

        assertEquals(1, count);

    }



//
//    @Test
//    public void createProjectTest() {
//        ProjectCreateRequest request1 = new ProjectCreateRequest()
//            .name("My Demo Project 1")
//            .description("This is a demo project created by TestNG 1");
//        ProjectCreateRequest request2 = new ProjectCreateRequest()
//            .name("My Demo Project 2")
//            .description("This is a demo project created by TestNG 2");
//        Projects projects = projectService.createProject(request1);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(1, projects.getProjects().size());
//
//        projects = projectService.createProject(request2);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(2, projects.getProjects().size());
//        assertNotNull(projects.getProjects().get(0).getProjectId());
//        assertEquals(request2.getName(), projects.getProjects().get(0).getName());
//        assertEquals(request2.getDescription(), projects.getProjects().get(0).getDescription());
//    }
//
//    @Test
//    public void getAllProjectsTest() {
//        ProjectCreateRequest request1 = new ProjectCreateRequest()
//            .name("My Demo Project 1")
//            .description("This is a demo project created by TestNG 1");
//        ProjectCreateRequest request2 = new ProjectCreateRequest()
//            .name("My Demo Project 2")
//            .description("This is a demo project created by TestNG 2");
//        Projects projects = projectService.createProject(request1);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(1, projects.getProjects().size());
//
//        projects = projectService.createProject(request2);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(2, projects.getProjects().size());
//
//        projects = projectService.getAllProjects();
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(2, projects.getProjects().size());
//    }
//
//    @Test
//    public void updateProjectTest() {
//        ProjectCreateRequest request1 = new ProjectCreateRequest()
//            .name("My Demo Project 1")
//            .description("This is a demo project created by TestNG 1");
//        Projects projects = projectService.createProject(request1);
//        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest()
//            .projectId(projects.getProjects().get(0).getProjectId())
//            .name("Updated name")
//            .description("Updated description");
//        Projects updatedProjects = projectService.updateProject(updateRequest);
//        assertNotNull(updatedProjects);
//        assertEquals(updatedProjects.getProjects().get(0).getName(), updateRequest.getName());
//        assertEquals(updatedProjects.getProjects().get(0).getDescription(), updateRequest.getDescription());
//        assertEquals(updatedProjects.getProjects().get(0).getProjectId(), updateRequest.getProjectId());
//    }
//
//
//    @Test
//    public void createFunctionTest() {
//        createAndAssertFunctions();
//    }
//
//    @Test
//    public void forkTest() {
//        FHFunctions functions = createAndAssertFunctions();
//
//        // Create a new project and fork the function to that
//        ProjectCreateRequest projRequest = new ProjectCreateRequest()
//            .name("My Demo Project")
//            .description("This is a demo project created by TestNG");
//        Projects projects = projectService.createProject(projRequest);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(2, projects.getProjects().size());
//        String projectId = projects.getProjects().get(0).getProjectId();
//
//        CodeUpdateResult forkResult = projectService.forkCode(new ForkRequest()
//            .projectId(projectId)
//            .parentCodeId(functions.getFunctions().get(0).getCodeId()));
//        assertNotNull(forkResult);
//        assertNotNull(forkResult.getVersion());
//        assertNotNull(forkResult.getUid());
//        assertNotNull(forkResult.getUid());
//
//        CodeCellEntity forkedCell = codeCellRepo.findByUid(UUID.fromString(forkResult.getUid()));
//        assertNotNull(forkedCell);
//        assertNotNull(forkedCell.getParentId());
//        assertEquals(functions.getFunctions().get(0).getCodeId(), forkedCell.getParentId().toString());
//
//        CodeCellEntity parentCell = codeCellRepo
//            .findByUid(UUID.fromString(functions.getFunctions().get(0).getCodeId()));
//        assertEquals(1, parentCell.getForkCount().longValue());
//    }
//
//    @Test
//    public void updateFunctionTest() {
//        FHFunctions functions = createAndAssertFunctions();
//        FHFunction function = functions.getFunctions().get(0);
//        ProjectItemEntity projectItem = projectItemRepo.findByCodeId(UUID.fromString(function.getCodeId()));
//        FHFunction functionToUpdate = new FHFunction().tags("apple, google, facebook")
//            .projectId(projectItem.getProjectId().toString())
//            .codeId(function.getCodeId());
//        functions = projectService.updateFunction(functionToUpdate);
//        assertNotNull(functions);
//        assertEquals(1, functions.getFunctions().size());
//        FHFunction result = functions.getFunctions().get(0);
//        assertEquals(functionToUpdate.getTags(), result.getTags());
//        assertTrue(result.getUpdatedAt() >= result.getCreatedAt());
//    }
//
//    @Test
//    public void getAllPublicFunctionsTest() {
//        int numFunctions = 12;
//        int numPublic = 6;
//        int numProjects = 3;
//        createMultipleFunctions(numFunctions, numPublic, numProjects, 0);
//
//        int totalPublicFunctions = numProjects * numPublic;
//        PageableResponse response = projectService.getAllPublicFunctions(new PageableRequest().pageNum(0)
//            .limit(Integer.MAX_VALUE));
//        assertNotNull(response);
//        assertEquals(1, (int) response.getNumPages());
//        assertEquals(totalPublicFunctions, (long) response.getTotalRecords());
//        assertEquals(totalPublicFunctions, response.getRecords().size());
//        assertFHFunctions(response.getRecords());
//
//        response = projectService.getAllPublicFunctions(new PageableRequest()
//            .pageNum(0)
//            .limit(2));
//        assertNotNull(response);
//        assertEquals(9, (int) response.getNumPages());
//        assertEquals(totalPublicFunctions, (long) response.getTotalRecords());
//        assertEquals(2, response.getRecords().size());
//        assertFHFunctions(response.getRecords());
//    }
//
//    @Test
//    public void searchAllPublicFunctionsTest() {
//        int numFunctions = 12;
//        int numPublic = 6;
//        int numProjects = 3;
//        int numWithTags = 4;
//        createMultipleFunctions(numFunctions, numPublic, numProjects, numWithTags);
//
//        PageableResponse response = projectService.getAllPublicFunctions(new PageableRequest()
//            .pageNum(0)
//            .query("facebook")
//            .limit(Integer.MAX_VALUE));
//        assertNotNull(response);
//        int totalTaggedFunctions = numWithTags * numProjects;
//        assertEquals(1, (int) response.getNumPages());
//        assertEquals(totalTaggedFunctions, (long) response.getTotalRecords());
//        assertEquals(totalTaggedFunctions, response.getRecords().size());
//
//        response = projectService.getAllPublicFunctions(new PageableRequest()
//            .pageNum(0)
//            .query("apple")
//            .limit(3));
//        assertNotNull(response);
//        assertEquals(4, (int) response.getNumPages());
//        assertEquals(totalTaggedFunctions, (long) response.getTotalRecords());
//        assertEquals(3, response.getRecords().size());
//
//        response = projectService.getAllPublicFunctions(new PageableRequest()
//            .pageNum(4)
//            .query("custom_greeter")
//            .limit(4));
//        assertNotNull(response);
//        assertEquals(5, (int) response.getNumPages());
//        assertEquals(18, (long) response.getTotalRecords());
//        assertEquals(2, response.getRecords().size());
//        assertFHFunctions(response.getRecords());
//
//        response = projectService.getAllPublicFunctions(new PageableRequest()
//            .pageNum(4)
//            .query("CUSTOM_GREETER")
//            .limit(4));
//        assertNotNull(response);
//        assertEquals(5, (int) response.getNumPages());
//        assertEquals(18, (long) response.getTotalRecords());
//        assertEquals(2, response.getRecords().size());
//        assertFHFunctions(response.getRecords());
//    }
//
//    private void assertFHFunctions(List<FHFunction> records) {
//        assertTrue(records.size() > 0);
//        for (FHFunction record : records) {
//            assertNotNull(record.getOwnerId());
////            assertNotNull(record.getOwnerUsername());
////            assertNotNull(record.getOwnerAvatar());
////            assertTrue(record.getOwnerAvatar().startsWith("https"));
//            assertNotNull(record.getOwnerId());
//            assertNotNull(record.getSlug());
//            assertNotNull(record.getName());
//            assertNotNull(record.getIsPublic());
//            assertNotNull(record.getCreatedAt());
//            assertNotNull(record.getUpdatedAt());
//        }
//    }
//
//    private void createMultipleFunctions(int numFunctions, int numPublic, int numProjects, int numWithTags) {
//        for (int i = 0; i < numProjects; i++) {
//            ProjectCreateRequest request1 = new ProjectCreateRequest()
//                .name("My Demo Project " + i)
//                .description("This is a demo project created by TestNG " + 1);
//            Projects projects = projectService.createProject(request1);
//            String projectId = projects.getProjects().get(0).getProjectId();
//            int numPrivateFunctions = numFunctions - numPublic;
//            for (int j = 0; j < numPublic; j++) {
//                projectService.createFunction(projectId);
//            }
//            int tagged = numWithTags;
//            // Toggle all these functions to public
//            for (FHFunction function : projectService.getAllFunctions(projectId).getFunctions()) {
//                Code code = new Code().isPublic(true).uid(function.getCodeId())
//                    .fieldsToUpdate(List.of("is_public"));
//                runtimeService.updateCode(code);
//                if (tagged > 0) {
//                    projectService.updateFunction(
//                        new FHFunction().tags("apple, google, facebook")
//                            .projectId(projectId)
//                            .codeId(function.getCodeId()));
//                    tagged--;
//                }
//            }
//
//            // Create private functions
//            for (int j = 0; j < numPrivateFunctions; j++) {
//                projectService.createFunction(projectId);
//            }
//        }
//
//        long totalFunctionCount = (long) numFunctions * numProjects;
//        assertEquals(numProjects, projectRepo.count());
//        assertEquals(totalFunctionCount, projectItemRepo.count());
//        assertEquals(totalFunctionCount, codeCellRepo.count());
//    }
//
//    private FHFunctions createAndAssertFunctions() {
//        ProjectCreateRequest request1 = new ProjectCreateRequest()
//            .name("My Demo Project 1")
//            .description("This is a demo project created by TestNG 1");
//        Projects projects = projectService.createProject(request1);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(1, projects.getProjects().size());
//
//        CodeUpdateResult result = projectService.createFunction(projects.getProjects().get(0).getProjectId());
//        assertNotNull(result);
//        assertNotNull(result.getSlug());
//        assertNotNull(result.getUid());
//        assertNotNull(result.getVersion());
//
//        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
//        assertNotNull(functions);
//        assertEquals(1, functions.getFunctions().size());
//        assertNotNull(functions.getFunctions().get(0).getOwnerId());
//        assertNotNull(functions.getFunctions().get(0).getSlug());
//        assertNotNull(functions.getFunctions().get(0).getName());
//        assertNotNull(functions.getFunctions().get(0).getIsPublic());
//        assertNotNull(functions.getFunctions().get(0).getCreatedAt());
//        assertNotNull(functions.getFunctions().get(0).getUpdatedAt());
//        return functions;
//    }
//
//    @Test
//    public void deleteFunctionTest() {
//        ProjectCreateRequest request1 = new ProjectCreateRequest()
//            .name("My Demo Project 1")
//            .description("This is a demo project created by TestNG 1");
//        Projects projects = projectService.createProject(request1);
//        CodeUpdateResult result = projectService.createFunction(projects.getProjects().get(0).getProjectId());
//        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
//        assertNotNull(functions);
//        assertEquals(1, functions.getFunctions().size());
//        functions = projectService.deleteFunction(functions.getFunctions().get(0).getSlug());
//        assertNotNull(functions);
//        assertEquals(0, functions.getFunctions().size());
//        assertNull(codeCellRepo.findBySlug(result.getSlug()));
//        assertEquals(0, projectItemRepo.findByProjectIdOrderByCreatedAtDesc(UUID.fromString(projects.getProjects().get(0).getProjectId())).size());
//    }
//
//    @Test
//    public void deleteProjectTest() {
//        ProjectCreateRequest request1 = new ProjectCreateRequest()
//            .name("My Demo Project 1")
//            .description("This is a demo project created by TestNG 1");
//        Projects projects = projectService.createProject(request1);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(1, projects.getProjects().size());
//
//        ProjectCreateRequest request2 = new ProjectCreateRequest()
//            .name("My Demo Project 2")
//            .description("This is a demo project created by TestNG 2");
//        projects = projectService.createProject(request2);
//        assertNotNull(projects);
//        assertNotNull(projects.getProjects());
//        assertEquals(2, projects.getProjects().size());
//
//        CodeUpdateResult result = projectService.createFunction(projects.getProjects().get(0).getProjectId());
//        assertNotNull(result);
//        assertNotNull(result.getSlug());
//        assertNotNull(result.getUid());
//        assertNotNull(result.getVersion());
//
//        FHFunctions functions = projectService.getAllFunctions(projects.getProjects().get(0).getProjectId());
//        assertNotNull(functions);
//        assertEquals(1, functions.getFunctions().size());
//
//        Projects remainingProjects = projectService.deleteProject(projects.getProjects().get(0).getProjectId());
//        assertNotNull(remainingProjects);
//        assertEquals(1, remainingProjects.getProjects().size());
//        assertEquals(projects.getProjects().get(1).getProjectId(),
//            remainingProjects.getProjects().get(0).getProjectId());
//
//        assertNull(codeCellRepo.findBySlug(result.getSlug()));
//        assertEquals(0, projectItemRepo.findByProjectIdOrderByCreatedAtDesc(UUID.fromString(projects.getProjects().get(0).getProjectId())).size());
//    }
}
