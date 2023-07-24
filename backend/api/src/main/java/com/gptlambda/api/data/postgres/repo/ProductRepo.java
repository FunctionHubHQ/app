package com.gptlambda.api.data.postgres.repo;


import com.gptlambda.api.data.postgres.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface ProductRepo extends JpaRepository<ProductEntity, Long> {
  ProductEntity findByProductSku(String sku);

//    @Query(value = "SELECT num_reviews " +
//      "FROM product " +
//      "WHERE product_sku = ?1",
//      nativeQuery = true)
//  Long findNumReviews(String sku);
}
