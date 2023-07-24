package com.gptlambda.api.data.postgres.repo;


import com.gptlambda.api.data.postgres.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface FeedbackRepo extends JpaRepository<FeedbackEntity, Long> {
}
