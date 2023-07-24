package com.gptlambda.api.service.embedding;

import com.gptlambda.api.data.postgres.projection.EmbeddingProjection;
import com.gptlambda.api.data.postgres.repo.EmbeddingRepo;
import com.gptlambda.api.data.postgres.repo.ProductAnswerRepo;
import com.gptlambda.api.data.postgres.repo.ProductQuestionRepo;
import com.gptlambda.api.data.postgres.repo.ProductRepo;
import com.gptlambda.api.data.postgres.repo.ReviewRepo;
import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.service.openai.OpenAiService;
import com.gptlambda.api.service.openai.embedding.EmbeddingResult;
import com.gptlambda.api.service.parser.AmazonParser;
import com.gptlambda.api.data.postgres.entity.EmbeddingEntity;
import com.gptlambda.api.data.postgres.entity.ProductAnswerEntity;
import com.gptlambda.api.data.postgres.entity.ProductEntity;
import com.gptlambda.api.data.postgres.entity.ProductQuestionEntity;
import com.gptlambda.api.data.postgres.entity.ReviewEntity;
import com.gptlambda.api.service.openai.embedding.Embedding;
import com.gptlambda.api.service.openai.embedding.EmbeddingRequest;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


/**
 * @author Biz Melesse created on 6/10/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

  private final ProductRepo productRepo;
  private final ReviewRepo reviewRepo;
  private final ProductQuestionRepo questionRepo;
  private final ProductAnswerRepo answerRepo;
  private final OpenAiProps openAiProps;
  private final EmbeddingRepo embeddingRepo;
  private final OpenAiService openAiService;
  private final int batchSize = 50;


  @Override
  public Double[] embedUserQuery(String query, String productSku) {
    List<String> input = List.of(query);
    EmbeddingRequest embeddingRequest = createEmbeddingRequest(productSku);
    embeddingRequest.setInput(input);
    EmbeddingResult result = getEmbeddingResult(embeddingRequest);
    if (result == null) {
      log.warn("embedUserQuery: Failed to get embeddings");
      return new Double[0];
    }
    if (result.getData().size() > 0) {
      List<Double> vector = result.getData().get(0).getEmbedding();
      return vector.toArray(new Double[0]);
    }
    return new Double[0];
  }

  @Override
  public List<String> fetchEmbeddings(String query, String productSku) {
    Double[] userQueryVector = embedUserQuery(query, productSku);
    List<EmbeddingProjection> embeddingsFound = embeddingRepo.findEmbeddings(
        Arrays.toString(userQueryVector), 0.5f, 5, productSku);
    return embeddingsFound
        .stream()
        .map(EmbeddingProjection::getContent)
        .collect(Collectors.toList());
  }

  @Override
  public void embedByProductSku(String productSku) {
    ProductEntity productEntity = productRepo.findByProductSku(productSku);
    if (productEntity == null) {
      log.warn("Product '{}' does not exist. Skipping embedding...", productSku);
      return;
    }
    if (!productEntity.getHasEmbedding()) {
      embedProduct(productEntity);
    }
    // Embed this product's reviews and Q & As
    Long productId = productEntity.getId();
    List<ReviewEntity> reviewsToEmbed = reviewRepo.findAllReviewsToEmbed(productId);
    List<ProductQuestionEntity> questionsToEmbed = questionRepo.findAllQuestionsToEmbed(productId);
    List<ProductAnswerEntity> answersToEmbed = answerRepo.findAllAnswersToEmbed(productId);
    try {
      Thread.sleep(2500L);
      Thread.startVirtualThread(() -> embedReviews(reviewsToEmbed, productSku));
      Thread.sleep(2500L);
      Thread.startVirtualThread(() -> embedQuestionsAndAnswers(questionsToEmbed, answersToEmbed, productSku));
    } catch (InterruptedException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  @Override
  public void embedProduct(ProductEntity productEntity) {
    // Split product embedding by the boundary
    List<String> input = new ArrayList<>(List.of(productEntity
        .getDescription().
        split(AmazonParser.bodyDelimiter)));
    input.add(productEntity.getTitle());
    double price = productEntity.getPrice();
    input.add(String.format("This product costs $%s. The price of this product is $%s. "
            + "How much it costs is $%s",
        price, price, price));
    input = input
        .stream()
        .map(this::clean)
        .collect(Collectors.toList());
    EmbeddingRequest embeddingRequest = createEmbeddingRequest(productEntity.getProductSku());
    embeddingRequest.setInput(input);
    log.info("Embedding product {}", productEntity.getProductSku());
    EmbeddingResult result = getEmbeddingResult(embeddingRequest);
    if (result == null) {
      log.warn("embedProduct: Failed to get embeddings");
      return; // TODO: should probably reschedule the job
    }

    List<EmbeddingEntity> embeddingEntities = new ArrayList<>();
    List<Embedding> data = result.getData();
    for (int i = 0; i <data.size(); i++) {
      List<Double> vector = data.get(i).getEmbedding();
      String text = input.get(i);
      EmbeddingEntity embedding = new EmbeddingEntity();
      embedding.setEmbedding(vector.toArray(new Double[0]));
      embedding.setReviewId(-1L);
      embedding.setQuestionId(-1L);
      embedding.setAnswerId(-1L);
      embedding.setProductId(productEntity.getId());
      embedding.setText(text);
      embedding.setProductSku(productEntity.getProductSku());
      embeddingEntities.add(embedding);
    }
    ProductEntity productEntityToUpdate = productRepo.findByProductSku(productEntity.getProductSku());
    productEntityToUpdate.setHasEmbedding(true);
    embeddingRepo.deleteAll(embeddingRepo.findAllProductDetailEmbeddings(productEntity.getId()));
    embeddingRepo.saveAll(embeddingEntities);
    productRepo.save(productEntityToUpdate);
  }

  @Override
  public void embedReviews(List<ReviewEntity> reviewsToEmbed, String productSku) {
    // Treat each review as a document. We don't need to truncate the reviews.
    // OpenAI's token limit for a single document is 8192. That is
    // about 6000 words, so it's safe to assume that no single review will have that many words.

    // Limit the batch size to 100
    ProductEntity productEntity = productRepo.findByProductSku(productSku);
    Long productId = productEntity.getId();
    List<List<?>> batches = getBatches(reviewsToEmbed);
    for (List<?> batch : batches) {
      log.info("Embedding a new batch of reviews - SKU={}", productSku);
      EmbeddingRequest embeddingRequest = createEmbeddingRequest(productSku);
      List<String> input = new ArrayList<>();
      List<Long> reviewIds = new ArrayList<>();
      for (Object o : batch) {
        ReviewEntity review = (ReviewEntity) o;
        String text = clean(review.getTitle() + " " + review.getBody());
        if (!ObjectUtils.isEmpty(text)) {
          reviewIds.add(review.getId());
          input.add(text);
        }
      }
      embeddingRequest.setInput(input);
      try {
        Thread.sleep(2_000);
      } catch (InterruptedException e) {
        log.error(e.getLocalizedMessage());
      }
      EmbeddingResult result = getEmbeddingResult(embeddingRequest);
      if (result == null) {
        log.warn("embedReviews: Failed to get embeddings");
        return; // TODO: should probably reschedule the job
      }

      // Save all the embeddings, along with the actual text and update the corresponding
      // reviews
      // Re-fetch the entities with new sessions
      Map<Long, ReviewEntity> reviewEntityMap = mapById(reviewRepo.findAllReviewsByIds(reviewIds));
      List<EmbeddingEntity> embeddingEntities = new ArrayList<>();
      List<Embedding> data = result.getData();
      for (int i = 0; i < result.getData().size(); i++) {
        List<Double> vector = data.get(i).getEmbedding();
        Long reviewId = reviewIds.get(i);
        String text = input.get(i);
        EmbeddingEntity embedding = new EmbeddingEntity();
        embedding.setEmbedding(vector.toArray(new Double[0]));
        embedding.setReviewId(reviewId);
        embedding.setProductId(productId);
        embedding.setQuestionId(-1L);
        embedding.setAnswerId(-1L);
        embedding.setText(text);
        embedding.setProductSku(productEntity.getProductSku());
        embeddingEntities.add(embedding);
        reviewEntityMap.get(reviewId).setHasEmbedding(true);
      }
      embeddingRepo.deleteAll(embeddingRepo.findAllReviewEmbeddingsByIds(reviewIds));
      embeddingRepo.saveAll(embeddingEntities);
      reviewRepo.saveAll(reviewEntityMap.values());
    }
  }

  private Map<Long, ReviewEntity> mapById(List<ReviewEntity> reviewEntities) {
    Map<Long, ReviewEntity> map = new HashMap<>();
    for (ReviewEntity reviewEntity : reviewEntities) {
      map.put(reviewEntity.getId(), reviewEntity);
    }
    return map;
  }

  private EmbeddingRequest createEmbeddingRequest(String productSku) {
    EmbeddingRequest request = new EmbeddingRequest();
    request.setUser(productSku);
    request.setModel(openAiProps.getEmbeddingModel());
    return request;
  }

  private String clean(String s) {
    return s
        .strip()
        .replaceAll("[^A-Za-z0-9. ]", ""); // Remove all non-alphanumeric characters
  }

  @Override
  public void embedQuestionsAndAnswers(List<ProductQuestionEntity> questionsToEmbed,
      List<ProductAnswerEntity> answersToEmbed, String productSku) {
    List<QAndA> qAndAS = inMemoryJoin(questionsToEmbed, answersToEmbed, productSku);
    List<List<?>> batches = getBatches(qAndAS);
    for (List<?> batch : batches) {
      log.info("Embedding a new batch of questions - SKU={}", productSku);
      EmbeddingRequest embeddingRequest = createEmbeddingRequest(productSku);
      List<String> input = new ArrayList<>();
      for (Object o : batch) {
        QAndA qAndA = (QAndA) o;
        input.add(String.format("%s %s",
            qAndA.getQuestion(), qAndA.getAnswer()));

      }
      embeddingRequest.setInput(input);
      EmbeddingResult result = getEmbeddingResult(embeddingRequest);
      if (result == null) {
        log.warn("embedQuestionsAndAnswers: Failed to get embeddings");
        return; // TODO: should probably reschedule the job
      }
      List<EmbeddingEntity> embeddingEntities = new ArrayList<>();
      List<Embedding> data = result.getData();
      for (int i = 0; i < data.size(); i++) {
        List<Double> vector = data.get(i).getEmbedding();
        String text = input.get(i);
        EmbeddingEntity embedding = new EmbeddingEntity();
        embedding.setEmbedding(vector.toArray(new Double[0]));
        embedding.setReviewId(-1L);
        embedding.setQuestionId(qAndAS.get(i).getQuestionId());
        embedding.setAnswerId(qAndAS.get(i).getAnswerId());
        embedding.setProductId(qAndAS.get(i).getProductId());
        embedding.setText(text);
        embedding.setProductSku(qAndAS.get(i).getProductSku());
        embeddingEntities.add(embedding);
      }
      List<Long> questionIds = qAndAS
          .stream()
          .map(QAndA::getQuestionId).toList();
      List<Long> answerIds = qAndAS
          .stream()
          .map(QAndA::getAnswerId).toList();
      List<ProductQuestionEntity> questionEntities = questionRepo.findQuestionsByIds(questionIds);
      List<ProductAnswerEntity> answerEntities = answerRepo.findAnswersByIds(answerIds);
      questionEntities = questionEntities.stream()
          .peek(it -> it.setHasEmbedding(true))
          .collect(Collectors.toList());
      answerEntities = answerEntities.stream()
          .peek(it -> it.setHasEmbedding(true))
          .collect(Collectors.toList());
      embeddingRepo.deleteAll(embeddingRepo.findAllQandAEmbeddingsByIds(answerIds));
      embeddingRepo.saveAll(embeddingEntities);
      questionRepo.saveAll(questionEntities);
      answerRepo.saveAll(answerEntities);
    }
  }

  @Override
  @Retry(name = "embeddings")
  public EmbeddingResult getEmbeddingResult(EmbeddingRequest embeddingRequest) {
    return openAiService.createEmbeddings(embeddingRequest);
  }

  private List<QAndA> inMemoryJoin(List<ProductQuestionEntity> questionsToEmbed,
      List<ProductAnswerEntity> answersToEmbed, String productSku) {
    Map<Long, ProductQuestionEntity> questionMap = new HashMap<>();
    for (ProductQuestionEntity q : questionsToEmbed) {
      questionMap.put(q.getId(), q);
    }
    List<QAndA> qAndAS = new ArrayList<>();
    for (ProductAnswerEntity answer : answersToEmbed) {
      ProductQuestionEntity question = questionMap.get(answer.getProductQuestionId());
      if (!ObjectUtils.isEmpty(question.getQuestion()) &&
      !ObjectUtils.isEmpty(answer.getAnswer())) {
        qAndAS.add(new QAndA(
            question.getQuestion(),
            answer.getAnswer(),
            question.getId(),
            answer.getId(),
            question.getProductId(),
            productSku));
      }
    }
    return qAndAS;
  }

  private List<List<?>> getBatches(List<?> entities) {
    List<List<?>> batches = new ArrayList<>();
    List<Object> batch = new ArrayList<>();
    for (Object entity : entities) {
      if (batch.size() >= batchSize) {
        batches.add(batch);
        batch.clear();
      }
      batch.add(entity);
    }
    if (batch.size() > 0) {
      batches.add(batch);
    }
    return batches;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  private static class QAndA {
    private String question;
    private String answer;
    private Long questionId;
    private Long answerId;
    private Long productId;
    private String productSku;
  }
}
