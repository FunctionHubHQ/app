package net.functionhub.api.data.postgres.repo;

import java.util.List;
import java.util.UUID;
import net.functionhub.api.data.postgres.entity.ProjectItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 7/26/23
 */
@Repository
@Transactional
public interface ProjectItemRepo extends JpaRepository<ProjectItemEntity, UUID> {
  List<ProjectItemEntity> findByProjectIdOrderByCreatedAtDesc(UUID uid);
  ProjectItemEntity findByCodeId(UUID uuid);
}
