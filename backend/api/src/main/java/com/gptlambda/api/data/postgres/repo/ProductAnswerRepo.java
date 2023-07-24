package com.gptlambda.api.data.postgres.repo;


import com.gptlambda.api.data.postgres.entity.ProductAnswerEntity;
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
public interface ProductAnswerRepo extends JpaRepository<ProductAnswerEntity, Long> {

  List<ProductAnswerEntity> findByProductId(Long productId);
  List<ProductAnswerEntity> findByProductQuestionId(Long questionId);

  @Query(value = "SELECT * " +
      "FROM product_answer " +
      "WHERE product_id = ?1 AND has_embedding = false",
      nativeQuery = true)
  List<ProductAnswerEntity> findAllAnswersToEmbed(Long productId);

  @Query(value = "SELECT * " +
      "FROM product_answer " +
      "WHERE id IN(?1)",
      nativeQuery = true)
  List<ProductAnswerEntity> findAnswersByIds(List<Long> answerIds);
}
