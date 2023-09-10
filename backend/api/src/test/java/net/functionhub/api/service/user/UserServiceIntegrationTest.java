package net.functionhub.api.service.user;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.functionhub.api.ApiKeyProvider;
import net.functionhub.api.ApiKeyRequest;
import net.functionhub.api.ApiKeyResponse;
import net.functionhub.api.data.postgres.entity.EntitlementEntity;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayPostgresMigration;
import net.functionhub.api.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Bizuwork Melesse
 * created on 2/13/22
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class UserServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserService userService;

    @Autowired
    private FlywayPostgresMigration flywayPostgresMigration;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ServiceTestHelper testHelper;

    @Autowired
    private EntitlementRepo entitlementRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeClass
    public void setup() {
        testHelper.prepareSecurity(null);
    }

    @AfterClass
    public void tearDown() {
    }

    @BeforeMethod
    public void beforeEachTest(Method method) {
        flywayPostgresMigration.migrate(true);
        log.info("  Testcase: " + method.getName() + " has started");
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void createEnvVariablesTest() {
        Map<String, Object> envVariables = new HashMap<>();
        envVariables.put("ALPHA_KEY", "alpha value");
        envVariables.put("BETA_KEY", "beta value");
        try {
            String encoded = userService.upsertEnvVariables(
                Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(envVariables)));
            assertNotNull(encoded);
            String decoded = new String(Base64.getDecoder().decode(encoded.getBytes()));
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> savedKeys = objectMapper.readValue(decoded, typeRef);
            assertNotNull(savedKeys);
            assertEquals(2, savedKeys.size());
            assertTrue(savedKeys.get("ALPHA_KEY").toString().startsWith("****************"));
            assertTrue(savedKeys.get("BETA_KEY").toString().startsWith("****************"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createUserTest() throws InterruptedException {
        UserProfileResponse response = userService.getOrCreateUserprofile();
        assertThat(response, is(notNullValue()));
        assertThat(response.getProfile().getName(), is(notNullValue()));
        assertThat(response.getProfile().getEmail(), is(notNullValue()));
        assertThat(response.getProfile().getUserId(), is(notNullValue()));
        Thread.sleep(5000);
        List<UserEntity> allUsers = userRepo.findAll();
        assertThat(allUsers, is(notNullValue()));
        assertThat(allUsers.size(), is(equalTo(1)));
        UserEntity user = allUsers.get(0);
        assertThat(user.getCreatedAt(), is(notNullValue()));
        assertThat(user.getUpdatedAt(), is(notNullValue()));
        assertThat(user.getEmail(), startsWith(response.getProfile().getEmail()));
        assertThat(user.getIsPremiumUser(), is(notNullValue()));

        EntitlementEntity entitlements = entitlementRepo.findByUserId(user.getId());
        assertThat(entitlements, is(notNullValue()));
        assertThat(entitlements.getMaxCpuTime(), is(equalTo(10L)));
        assertThat(entitlements.getMaxExecutionTime(), is(equalTo(30000L)));
        assertThat(entitlements.getMaxMemoryUsage(), is(equalTo(134217728L)));
        assertThat(entitlements.getMaxDataTransfer(), is(equalTo(104857600L)));
        assertThat(entitlements.getMaxFunctions(), is(equalTo(10L)));
        assertThat(entitlements.getMaxFunctions(), is(equalTo(10L)));
        assertThat(entitlements.getMaxHttpCalls(), is(equalTo(10L)));
        assertThat(entitlements.getMaxProjects(), is(equalTo(1L)));
    }

    @Test
    public void generateApiKeyTest() {
        ApiKeyResponse response = userService.createNewApiKey(new ApiKeyRequest());
        assertNotNull(response);
        assertNotNull(response.getKeys());
        assertEquals(1, response.getKeys().size());
        assertFalse(response.getKeys().get(0).getKey().contains("*"));
    }

    @Test
    public void upsertVendorKeyTest() {
        ApiKeyResponse response = userService.createNewApiKey(
            new ApiKeyRequest()
                .key(UUID.randomUUID().toString())
                .provider(ApiKeyProvider.OPEN_AI));
        assertNotNull(response);
        assertNotNull(response.getKeys());
        assertEquals(1, response.getKeys().size());
        assertTrue(response.getKeys().get(0).getKey().contains("*"));

        // Generate function hub key
        response = userService.createNewApiKey(new ApiKeyRequest());
        assertNotNull(response);
        assertNotNull(response.getKeys());
        assertEquals(2, response.getKeys().size());
        assertFalse(response.getKeys().get(0).getKey().contains("*"));
    }

    @Test
    public void deleteKeyTest() {
        // generate 10 keys
        ApiKeyResponse response = null;
        for (int i = 0; i < 10; i++) {
            response = userService.createNewApiKey(new ApiKeyRequest());
        }
        assertNotNull(response);
        assertEquals(10, response.getKeys().size());

        final ApiKeyResponse initialResponse = response;
        ApiKeyResponse postDeleteResponse = userService.deleteKey(
            new ApiKeyRequest().key(response.getKeys().get(0).getKey()));
        assertNotNull(postDeleteResponse);
        assertEquals(9, postDeleteResponse.getKeys().size());
        assertEquals(0, postDeleteResponse.getKeys()
            .stream()
            .filter(it -> it.getKey().equals(initialResponse.getKeys().get(0).getKey()))
            .toList().size());
    }

    @Test
    public void requireAtLeastOneApiKeyTest() {
        // generate 1 key
        ApiKeyResponse response = userService.createNewApiKey(new ApiKeyRequest());
        assertNotNull(response);
        assertEquals(1, response.getKeys().size());

        final ApiKeyResponse initialResponse = response;
        ApiKeyResponse postDeleteResponse = userService.deleteKey(
            new ApiKeyRequest().key(response.getKeys().get(0).getKey()));
        assertNotNull(postDeleteResponse);
        assertEquals(1, postDeleteResponse.getKeys().size());
        assertEquals(0, postDeleteResponse.getKeys()
            .stream()
            .filter(it -> it.getKey().equals(initialResponse.getKeys().get(0).getKey()))
            .toList().size());
    }
}
