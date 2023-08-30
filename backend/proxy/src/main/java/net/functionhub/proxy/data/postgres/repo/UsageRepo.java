package net.functionhub.proxy.data.postgres.repo;


import java.util.UUID;
import net.functionhub.api.data.postgres.entity.UsageEntity;
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
