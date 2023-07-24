package com.gptlambda.api.service.scraper;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.gptlambda.api.data.postgres.entity.ProductAnswerEntity;
import com.gptlambda.api.data.postgres.entity.ProductEntity;
import com.gptlambda.api.data.postgres.entity.ProductQuestionEntity;
import com.gptlambda.api.data.postgres.entity.ReviewEntity;
import com.gptlambda.api.data.postgres.repo.ProductAnswerRepo;
import com.gptlambda.api.data.postgres.repo.ProductQuestionRepo;
import com.gptlambda.api.data.postgres.repo.ProductRepo;
import com.gptlambda.api.data.postgres.repo.ReviewRepo;
import com.gptlambda.api.service.ServiceTestConfiguration;
import com.gptlambda.api.service.parser.AmazonParser;
import com.gptlambda.api.utils.ServiceTestHelper;
import com.gptlambda.api.utils.migration.FlywayMigration;
import java.lang.reflect.Method;
import java.util.List;
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
 * created on 5/26/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class ScraperServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private AmazonParser amazonParser;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private FlywayMigration flywayMigration;

    @Autowired
    private ProductQuestionRepo productQuestionRepo;

    @Autowired
    private ProductAnswerRepo productAnswerRepo;

    @BeforeClass
    public void setup() {
    }

    @AfterTest
    public void teardown() {
    }


    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
        flywayMigration.migrate(true);
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void amazonElectronicsProductPageParserTest() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        String soup = ServiceTestHelper.loadFromFile("scraper/amazonProductPage_Electronics.html");
        amazonParser.parseProductPage(sku, soup, false, null);
        assertProduct(sku, 99.68);
    }

    @Test
    public void amazonClothingProductPageParserTest() {
        String sku = "B0BJDTKPY1";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        String soup = ServiceTestHelper.loadFromFile("scraper/amazonProductPage_Clothing.html");
        amazonParser.parseProductPage(sku, soup, false, null);
        assertProduct(sku, 25.99);

    }

    @Test
    public void amazonBookProductPageParserTest() {
        String sku = "0385347863";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        String soup = ServiceTestHelper.loadFromFile("scraper/amazonProductPage_Books.html");
        amazonParser.parseProductPage(sku, soup, false, null);
        assertProduct(sku, 14.49);
    }

    @Test
    public void generateReviewLinksTest() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        long numReviews = 2679;
        String soup = ServiceTestHelper.loadFromFile("scraper/amazonProductPage_Electronics.html");
        amazonParser.parseProductPage(sku, soup, false, null);
        List<String> reviewLinks = amazonParser.generateReviewLinks(
            productRepo.findByProductSku(sku), "five_star", numReviews);
        assertEquals(numReviews, reviewLinks.size());
        for (String reviewLink : reviewLinks) {
            assertTrue(reviewLink.contains("amazon.com"));
            assertTrue(reviewLink.contains(sku));
            assertTrue(reviewLink.contains("product-reviews"));
        }
    }
    
    @Test
    public void generateProductQuestionLinksTest() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        String soup = ServiceTestHelper.loadFromFile("scraper/amazonProductPage_Electronics.html");
        amazonParser.parseProductPage(sku, soup, false, null);
        List<String> questionLinks = amazonParser.generateProductQuestionLinks(productRepo.findByProductSku(sku));
        assertEquals(90, questionLinks.size());
        for (String reviewLink : questionLinks) {
            assertTrue(reviewLink.contains("amazon.com"));
            assertTrue(reviewLink.contains(sku));
            assertTrue(reviewLink.contains("isAnswered=true"));
        }
    }

    @Test
    public void generateProductAnswerLinksTest() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);

        ProductQuestionEntity questionEntity = new ProductQuestionEntity();
        questionEntity.setQuestion("We just purchased monitor-unable to hear through the built in speakers. Do we need a certain type of audio pin/cord? If so which one?");
        questionEntity.setProductId(entity.getId());
        questionEntity.setQuestionId("Tx10HRF7ZMGOPTD");
        questionEntity.setNumAnswers(12L);
        productQuestionRepo.save(questionEntity);
        List<String> answerLinks = amazonParser.generateAnswerLinks(questionEntity.getQuestionId());
        assertEquals(2, answerLinks.size());
        for (String reviewLink : answerLinks) {
            assertTrue(reviewLink.contains("amazon.com"));
            assertTrue(reviewLink.contains(questionEntity.getQuestionId()));
            assertTrue(reviewLink.contains("questions"));
        }
    }

    @Test
    public void parseReviewPageTest() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        String soup = ServiceTestHelper.loadFromFile("scraper/AmazonProductReviewPage.html");
        amazonParser.parseReviewPage(entity, soup, false, "", 0);

        List<ReviewEntity> reviews = reviewRepo.findAllByProductId(entity.getId());
        assertEquals(10, reviews.size());
        for (ReviewEntity review : reviews) {
            assertNotNull(review.getProductId());
            assertNotNull(review.getTitle());
            assertNotNull(review.getReviewId());
            assertNotNull(review.getMerchant());
            assertNotNull(review.getCountry());
            assertNotNull(review.getUpvotes());
            assertNotNull(review.getStarRating());
            assertNotNull(review.getReviewer());
            assertNotNull(review.getBody());
            assertNotNull(review.getSubmittedAt());
        }
    }

    @Test
    public void parseProductQuestionPage() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);
        String soup = ServiceTestHelper.loadFromFile("scraper/AmazonProductQuestionPage.html");
        Long productId = entity.getId();
        amazonParser.parseProductQuestions(entity, soup, false);
        List<ProductQuestionEntity> questionEntities = productQuestionRepo.findByProductId(productId);
        List<ProductAnswerEntity> answerEntities = productAnswerRepo.findByProductId(productId);
        assertEquals(questionEntities.size(), 10);
        assertEquals(answerEntities.size(), 10);
        for (ProductQuestionEntity questionEntity : questionEntities) {
            assertNotNull(questionEntity.getQuestionId());
            assertNotNull(questionEntity.getQuestion());
            assertNotNull(questionEntity.getUpvotes());
        }
        for (ProductAnswerEntity answerEntity : answerEntities) {
            assertNotNull(answerEntity.getAnswer());
            assertNotNull(answerEntity.getAnsweredBy());
            assertNotNull(answerEntity.getAnsweredAt());
            assertNotNull(answerEntity.getUpvotes());
        }
    }

    @Test
    public void parseProductAnswerPage() {
        String sku = "B0773ZY26F";
        ProductEntity entity = new ProductEntity();
        entity.setProductSku(sku);
        entity.setNumReviews(0L);
        entity.setStarRating(0.0);
        productRepo.save(entity);

        amazonParser.parseProductQuestions(entity, ServiceTestHelper.loadFromFile("scraper/AmazonProductQuestionPage.html"),
            false);

        String soup = ServiceTestHelper.loadFromFile("scraper/AmazonProductAnswerPage.html");
        String questionId = "Tx1C9YPPCCBMZI8";
        ProductQuestionEntity productQuestionEntity = productQuestionRepo.findByQuestionId(questionId);
        amazonParser.parseProductAnswers(questionId, soup);

        List<ProductAnswerEntity> answers = productAnswerRepo.findByProductQuestionId(productQuestionEntity.getId());
        assertEquals(answers.size(), 11);
        for (ProductAnswerEntity answer : answers) {
            assertNotNull(answer.getAnswer());
            assertNotNull(answer.getAnsweredAt());
            assertNotNull(answer.getAnsweredBy());
        }
    }

    private void assertProduct(String sku, double price) {
        ProductEntity entity = productRepo.findByProductSku(sku);
        assertNotNull(entity);
        assertNotNull(entity.getDescription());
        assertNotNull(entity.getNumReviews());
        assertNotNull(entity.getStarRating());
        assertTrue(entity.getStarRating() >= 4.0);
        assertEquals(price, entity.getPrice());
    }
}
