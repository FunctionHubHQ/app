package com.gptlambda.api.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.gptlambda.api.data.postgres.repo.ProductRepo;
import com.gptlambda.api.ProductCreateResponse;
import com.gptlambda.api.ProductFromDataRequest;
import com.gptlambda.api.ProductRequest;
import com.gptlambda.api.data.postgres.entity.ProductEntity;
import com.gptlambda.api.dto.JobType;
import com.gptlambda.api.dto.ScrapingJob;
import com.gptlambda.api.props.RabbitMQProps;
import com.gptlambda.api.service.chromeExtension.ExtensionServiceImpl.MessageType;
import com.gptlambda.api.service.parser.AmazonParser;
import com.gptlambda.api.service.utils.ShoppiemUtils;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 5/25/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepo productRepo;
  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMQProps rabbitMQProps;
  private final AmazonParser amazonParser;

  @Override
  public ProductCreateResponse createProductFromData(ProductFromDataRequest productFromDataRequest) {
    var url = cleanupUrl(productFromDataRequest.getProductUrl());
    var productSku = parseProductSku(url);
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null) {
      entity = new ProductEntity();
      entity.setProductSku(productSku);
      entity.setNumReviews(productFromDataRequest.getNumReviews());
      entity.setStarRating(productFromDataRequest.getStarRating());
      entity.setNumQuestionsAnswered(productFromDataRequest.getNumQuestionsAnswered());
      entity.setCurrency(productFromDataRequest.getCurrency());
      entity.setImageUrl(productFromDataRequest.getImageUrl());
      entity.setPrice(productFromDataRequest.getPrice());
      entity.setProductUrl(url);
      entity.setBaseUrl(getBaseUrl(url));
      entity.setSeller(productFromDataRequest.getSeller());
      entity.setTitle(ShoppiemUtils.truncate(productFromDataRequest.getTitle()));
      entity.setDescription(productFromDataRequest.getDescription());
      entity.setIsReady(false);
      productRepo.save(entity);
    }
    if (entity.getIsReady()) {
      return new ProductCreateResponse()
          .isReady(true);
    }
    return new ProductCreateResponse()
        .inProgress(true);
  }

  @Override
  public ProductCreateResponse createProduct(ProductRequest productRequest, String fcmToken) {
    var url = cleanupUrl(productRequest.getProductUrl());
    var productSku = parseProductSku(url);
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null ||
        ObjectUtils.isEmpty(entity.getTitle()) ||
        ObjectUtils.isEmpty(entity.getDescription())) {
      if (entity == null) {
        entity = new ProductEntity();
      }
      entity.setProductSku(productSku);
      entity.setBaseUrl(getBaseUrl(url));
      entity.setProductUrl(url);
      productRepo.save(entity);
      if (!ObjectUtils.isEmpty(productRequest.getHtml())) {
        amazonParser.parseProductPage(productSku, productRequest.getHtml(), true, fcmToken);
      } else {
        ScrapingJob job = new ScrapingJob();
        job.setProductSku(productSku);
        job.setId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_UID_LENGTH));
        job.setUrl(url);
        job.setType(JobType.PRODUCT_PAGE);
        try {
          String jobString = objectMapper.writeValueAsString(job);
          rabbitTemplate.convertAndSend(
              rabbitMQProps.getTopicExchange(),
              rabbitMQProps
                  .getJobQueues()
                  .get(RabbitMQProps.SCRAPE_JOB_QUEUE_KEY)
                  .getRoutingKeyPrefix() + productSku,
              jobString);
          return new ProductCreateResponse()
              .inProgress(true);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
          return new ProductCreateResponse()
              .error(e.getLocalizedMessage());
        }
      }
    } else {
      sendProductInfoToClient(entity, productSku, fcmToken);
    }
    if (entity.getIsReady()) {
      return new ProductCreateResponse()
          .productSku(productSku)
          .isReady(true);
    }
    return new ProductCreateResponse()
        .productSku(productSku)
        .inProgress(true);
  }

  @Override
  public void sendProductInfoToClient(ProductEntity entity, String productSku, String fcmToken) {
    if (entity == null) {
      entity = productRepo.findByProductSku(productSku);
    }
    if (entity != null && !ObjectUtils.isEmpty(fcmToken)) {
      try {
        Message message = Message.builder()
            .putData("name", entity.getTitle())
            .putData("imageUrl", entity.getImageUrl())
            .putData("productSku", productSku)
            .putData("type", MessageType.PRODUCT_INFO_REQUEST)
            .setToken(fcmToken)
            .build();
        FirebaseMessaging.getInstance().send(message);
      } catch (Exception e) {
        // pass
      }
    }
  }

  private String cleanupUrl(String url) {
    try {
      URL parsedUrl = new URL(url);
      return String.format("%s://%s%s", parsedUrl.getProtocol(), parsedUrl.getHost(), parsedUrl.getPath());
    } catch (MalformedURLException e) {
      return url;
    }
  }

  private String getBaseUrl(String url) {
    try {
      URL parsedUrl = new URL(url);
      return String.format("%s://%s", parsedUrl.getProtocol(), parsedUrl.getHost());
    } catch (MalformedURLException e) {
      return url;
    }
  }

  @Override
  public String parseProductSku(String url) {
    // Assume we are only dealing with Amazon product URLs
    String[] tokens = url.split("/dp/");
    String skuPart = tokens[1];
    String sku = skuPart.split("/")[0];
    return sku;
  }
}
