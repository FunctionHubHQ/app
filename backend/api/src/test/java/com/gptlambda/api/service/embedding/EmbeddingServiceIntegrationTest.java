//package com.gptlambda.api.service.embedding;
//
//
//import static org.testng.AssertJUnit.assertEquals;
//import static org.testng.AssertJUnit.assertNotNull;
//import static org.testng.AssertJUnit.assertTrue;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gptlambda.api.service.ServiceTestConfiguration;
//import com.gptlambda.api.utils.ServiceTestHelper;
//import com.gptlambda.api.utils.migration.FlywayMigration;
//import java.lang.reflect.Method;
//import java.util.List;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.AfterTest;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Ignore;
//import org.testng.annotations.Test;
//
///**
// * @author Bizuwork Melesse
// * created on 6/11/23
// */
//@Ignore
//@Slf4j
//@SpringBootTest(classes = ServiceTestConfiguration.class)
//public class EmbeddingServiceIntegrationTest extends AbstractTestNGSpringContextTests {
//
//    @Autowired
//    private EmbeddingService embeddingService;
//
//    @Autowired
//    private ReviewRepo reviewRepo;
//
//    @Autowired
//    private EmbeddingRepo embeddingRepo;
//
//    @Autowired
//    private ProductRepo productRepo;
//
//    @Autowired
//    private ProductQuestionRepo questionRepo;
//
//    @Autowired
//    private ProductAnswerRepo answerRepo;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private FlywayMigration flywayMigration;
//
//    @BeforeClass
//    public void setup() {
//        flywayMigration.migrate(true);
//      ServiceTestHelper.loadProducts(true, objectMapper, productRepo);
//      ServiceTestHelper.loadEmbeddings(objectMapper, embeddingRepo);
//      ProductEntity entity = productRepo.findById(1L).get();
//      entity.setHasEmbedding(true);
//      productRepo.save(entity);
//    }
//
//    @AfterTest
//    public void teardown() {
//    }
//
//    @BeforeMethod
//    public void beforeEachTest(Method method) {
//        log.info("  Testcase: " + method.getName() + " has started");
//    }
//
//    @AfterMethod
//    public void afterEachTest(Method method) {
//        log.info("  Testcase: " + method.getName() + " has ended");
//    }
//
//    @Test(enabled = false)
//    public void createReviewEmbeddingsTest() throws InterruptedException {
//        List<ReviewEntity> reviews = reviewRepo
//            .findAllReviewsByIds(List.of(1L, 2L, 3L, 4L, 5L));
//        embeddingService.embedReviews(reviews, "B0BW8K69VP");
//      Thread.sleep(5000);
//      List<Long> reviewIds = reviews.stream().map(ReviewEntity::getId).toList();
//      reviews = reviewRepo.findAllReviewsByIds(reviewIds);
//      assertEquals(reviewIds.size(), reviews.size());
//      for (ReviewEntity review : reviews) {
//            assertTrue(review.getHasEmbedding());
//      }
//      List<EmbeddingEntity> embeddings = embeddingRepo.findAllReviewEmbeddingsByIds(reviewIds);
//      assertEquals(reviewIds.size(), embeddings.size());
//      assertEmbeddings(embeddings);
//    }
//
//  @Test
//  public void createProductDetailEmbeddingsTest() throws InterruptedException {
//      String sku = "B0BW8K69VP";
//      ProductEntity productEntity = productRepo.findByProductSku(sku);
//      if (embeddingRepo.findAllProductDetailEmbeddings(productEntity.getId()).size() == 0) {
//        embeddingService.embedProduct(productEntity);
//        Thread.sleep(5000);
//      }
//      productEntity = productRepo.findByProductSku(sku);
//      assertTrue(productEntity.getHasEmbedding());
//      List<EmbeddingEntity> embeddings = embeddingRepo.findAllProductDetailEmbeddings(productEntity.getId());
//      assertEmbeddings(embeddings);
//    }
//
//  @Test(enabled = false)
//  public void createQandAEmbeddingsTest() throws InterruptedException {
//    String sku = "B0773ZY26F";
//    List<Long> ids = List.of(161L, 162L, 163L, 164L, 165L);
//    List<ProductQuestionEntity> questionEntities = questionRepo.findQuestionsByIds(ids);
//    List<ProductAnswerEntity> answerEntities = answerRepo.findAnswersByIds(ids);
//    embeddingService.embedQuestionsAndAnswers(questionEntities, answerEntities, sku);
//    Thread.sleep(5000);
//    questionEntities = questionRepo.findQuestionsByIds(ids);
//    answerEntities = answerRepo.findAnswersByIds(ids);
//    for (ProductQuestionEntity questionEntity : questionEntities) {
//      assertTrue(questionEntity.getHasEmbedding());
//    }
//    for (ProductAnswerEntity answerEntity : answerEntities) {
//      assertTrue(answerEntity.getHasEmbedding());
//    }
//
//    List<EmbeddingEntity> embeddings = embeddingRepo.findAllQandAEmbeddingsByIds(ids);
//    assertEquals(embeddings.size(), ids.size());
//    assertEmbeddings(embeddings);
//  }
//
//  @Test(enabled = false)
//  public void embedProductBySkuTest() throws InterruptedException {
//    String sku = "B0773ZY26F";
//    embeddingService.embedByProductSku(sku);
//    Thread.sleep(600000);
//
//  }
//
//  private void assertEmbeddings(List<EmbeddingEntity> embeddings) {
//    assertTrue(embeddings.size() > 1);
//    for (EmbeddingEntity embedding : embeddings) {
//      assertNotNull(embedding.getEmbedding());
//      assertEquals(1536L, embedding.getEmbedding().length);
//    }
//  }
//}
