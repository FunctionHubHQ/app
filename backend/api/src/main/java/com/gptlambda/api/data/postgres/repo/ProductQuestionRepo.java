package com.gptlambda.api.data.postgres.repo;


import com.gptlambda.api.data.postgres.entity.ProductQuestionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 6/1/23
 */
@Repository
@Transactional
public interface ProductQuestionRepo extends JpaRepository<ProductQuestionEntity, Long> {
  ProductQuestionEntity findByQuestionId(String questionId);
  List<ProductQuestionEntity> findByProductId(Long productId);

  @Query(value = "SELECT * " +
      "FROM product_question " +
      "WHERE product_id = ?1 AND has_embedding = false",
      nativeQuery = true)
  List<ProductQuestionEntity> findAllQuestionsToEmbed(Long productId);

  @Query(value = "SELECT * " +
      "FROM product_question " +
      "WHERE id IN(?1)",
      nativeQuery = true)
  List<ProductQuestionEntity> findQuestionsByIds(List<Long> questionIds);
}
