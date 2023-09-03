package net.functionhub.api.service.entitlement;


import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.service.ServiceTestConfiguration;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.ServiceTestHelper;
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
public class EntitlementServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private EntitlementService entitlementService;

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceTestHelper testHelper;

    @BeforeClass
    public void setup() {
    }

    @AfterTest
    public void teardown() {
    }

    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
        String userId = "u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH);
        testHelper.prepareSecurity(userId);
        userService.getOrCreateUserprofile();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }


    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void createInvocationRecordTest() {
        int count = 100;
        for (int i = 0; i < count; i++) {
            entitlementService.recordFunctionInvocation();
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ignored) {
            }
        }
        long found = entitlementService.getNumFunctionInvocations(1);
        assertEquals(count, found);

    }
}
