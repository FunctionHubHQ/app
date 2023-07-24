package com.gptlambda.api.data.postgres.repo;

import com.gptlambda.api.data.postgres.entity.EmbeddingEntity;
import com.gptlambda.api.data.postgres.projection.EmbeddingProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 5/25/2023
 */
@Repository
@Transactional
public interface EmbeddingRepo extends JpaRepository<EmbeddingEntity, Long> {

  @Query(value = "SELECT COUNT (id) " +
      "FROM embedding " +
      "WHERE product_sku = ?1",
      nativeQuery = true)
  Long countDocumentsEmbedded(String productSku);

  @Query(value = "SELECT DISTINCT ON (review_id) * " +
      "FROM embedding " +
      "WHERE review_id IN(?1)",
      nativeQuery = true)
  List<EmbeddingEntity> findAllReviewEmbeddingsByIds(List<Long> reviewIds);

  @Query(value = "SELECT * " +
      "FROM embedding " +
      "WHERE answer_id IN(?1)",
      nativeQuery = true)
  List<EmbeddingEntity> findAllQandAEmbeddingsByIds(List<Long> answerIds);

  @Query(value = "SELECT * " +
      "FROM embedding " +
      "WHERE product_id = ?1 "
      + "AND review_id = -1 "
      + "AND question_id = -1 "
      + "AND answer_id = -1",
      nativeQuery = true)
  List<EmbeddingEntity> findAllProductDetailEmbeddings(Long productId);

  @Query(value = "SELECT "
      + "e.id AS id, "
      + "e.text AS content, "
      + "1 - (e.embedding <=> cast(:queryVector AS vector)) AS similarity "
      + "FROM embedding e "
      + "  WHERE (1 - (e.embedding <=> cast(:queryVector AS vector)) > :matchThreshold) "
      + "   AND e.product_sku = :userProductSku "
      + "  ORDER BY similarity DESC "
      + "  LIMIT :matchCount",
      nativeQuery = true)
  List<EmbeddingProjection> findEmbeddings(
      @Param("queryVector") String queryVector,
      @Param("matchThreshold") float matchThreshold,
      @Param("matchCount") int matchCount,
      @Param("userProductSku") String userProductSku);
}
