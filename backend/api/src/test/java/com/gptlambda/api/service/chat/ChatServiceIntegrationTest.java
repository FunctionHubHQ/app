package com.gptlambda.api.service.chat;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptlambda.api.data.postgres.repo.EmbeddingRepo;
import com.gptlambda.api.data.postgres.repo.ProductAnswerRepo;
import com.gptlambda.api.data.postgres.repo.ProductQuestionRepo;
import com.gptlambda.api.data.postgres.repo.ProductRepo;
import com.gptlambda.api.data.postgres.repo.ReviewRepo;
import com.gptlambda.api.utils.migration.FlywayMigration;
import com.gptlambda.api.service.ServiceTestConfiguration;
import com.gptlambda.api.utils.ServiceTestHelper;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
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
 * created on 6/12/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class ChatServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private EmbeddingRepo embeddingRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductQuestionRepo questionRepo;

    @Autowired
    private ProductAnswerRepo answerRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private FlywayMigration flywayMigration;


    @BeforeClass
    public void setup() {
        flywayMigration.migrate(true);
        ServiceTestHelper.loadProducts(true, objectMapper, productRepo);
        ServiceTestHelper.loadReviews(objectMapper, reviewRepo);
        ServiceTestHelper.loadQuestions(objectMapper, questionRepo);
        ServiceTestHelper.loadAnswers(objectMapper, answerRepo);
        ServiceTestHelper.loadEmbeddings(objectMapper, embeddingRepo);
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
    public void callGptTest() {
        String registrationToken = "APA91bE4sRfKX9swQU3g91_gFgeQciZg6-mP1V1SAdDBPjfbyg8tetPzmL9GZUQMfSjY3Lz2xspDbo9BAYRjw1M4qzqLsVVeeaGhyoVXPorZ9loQrdR0K6tQIFxOuSVM3GvqSCLTZ3FNLwVEYm12dBb4ZltDCLlAug";
        String query = "What is the average battery life?";
        query = "what is the screen resolution for gaming?";
        String sku = "B0BW8K69VP";
        chatService.callGpt(query, registrationToken, sku);
    }
}
