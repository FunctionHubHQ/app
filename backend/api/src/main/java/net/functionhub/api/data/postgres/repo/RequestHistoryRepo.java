package net.functionhub.api.data.postgres.repo;

import java.util.UUID;
import net.functionhub.api.data.postgres.entity.RequestHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 7/26/23
 */
@Repository
@Transactional
public interface RequestHistoryRepo extends JpaRepository<RequestHistoryEntity, UUID> {
}
