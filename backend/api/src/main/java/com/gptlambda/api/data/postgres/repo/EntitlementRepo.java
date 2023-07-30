package com.gptlambda.api.data.postgres.repo;

import com.gptlambda.api.data.postgres.entity.EntitlementEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 7/26/23
 */
@Repository
@Transactional
public interface EntitlementRepo extends JpaRepository<EntitlementEntity, UUID> {
  EntitlementEntity findByUserId(String uid);
}
