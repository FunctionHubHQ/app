package net.functionhub.api.service.user;


import net.functionhub.api.data.postgres.entity.EntitlementEntity;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.utils.ServiceTestHelper;
import net.functionhub.api.utils.migration.FlywayMigration;
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
    private FlywayMigration flywayMigration;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ServiceTestHelper testHelper;

    @Autowired
    private EntitlementRepo entitlementRepo;

    @BeforeClass
    public void setup() {
        testHelper.prepareSecurity(null);
        flywayMigration.migrate(true);
    }

    @AfterClass
    public void tearDown() {
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
    public void createUserTest() throws InterruptedException {
        UserProfileResponse response = userService.getOrCreateUserprofile();
        assertThat(response, is(notNullValue()));
        assertThat(response.getProfile().getName(), is(notNullValue()));
        assertThat(response.getProfile().getEmail(), is(notNullValue()));
        assertThat(response.getProfile().getUid(), is(notNullValue()));
        Thread.sleep(5000);
        List<UserEntity> allUsers = userRepo.findAll();
        assertThat(allUsers, is(notNullValue()));
        assertThat(allUsers.size(), is(equalTo(1)));
        UserEntity user = allUsers.get(0);
        assertThat(user.getCreatedAt(), is(notNullValue()));
        assertThat(user.getUpdatedAt(), is(notNullValue()));
        assertThat(user.getEmail(), startsWith(response.getProfile().getEmail()));
        assertThat(user.getUid(), is(notNullValue()));

        EntitlementEntity entitlements = entitlementRepo.findByUserId(user.getUid());
        assertThat(entitlements, is(notNullValue()));
        assertThat(entitlements.getTimeout(), greaterThan(1000L));

    }
}
