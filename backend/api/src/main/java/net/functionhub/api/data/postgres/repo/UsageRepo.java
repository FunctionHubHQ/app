package net.functionhub.api.data.postgres.repo;


import net.functionhub.api.data.postgres.entity.UsageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface UsageRepo extends JpaRepository<UsageEntity, UUID> {
    UsageEntity findByUserId(String uid);
}
