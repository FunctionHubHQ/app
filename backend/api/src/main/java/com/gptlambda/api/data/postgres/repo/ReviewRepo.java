package com.gptlambda.api.data.postgres.repo;


import com.gptlambda.api.data.postgres.entity.ReviewEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface ReviewRepo extends JpaRepository<ReviewEntity, Long> {
  List<ReviewEntity> findAllByProductId(Long productId);

      @Query(value = "SELECT * " +
      "FROM review " +
      "WHERE product_id = ?1 AND has_embedding = false",
      nativeQuery = true)
      List<ReviewEntity> findAllReviewsToEmbed(Long productId);

  @Query(value = "SELECT * " +
      "FROM review " +
      "WHERE id IN(?1)",
      nativeQuery = true)
  List<ReviewEntity> findAllReviewsByIds(List<Long> reviewIds);


}
