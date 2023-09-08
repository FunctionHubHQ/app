package net.functionhub.api.data.postgres.repo;

import java.util.List;
import java.util.UUID;
import net.functionhub.api.data.postgres.entity.ProjectEntity;
import net.functionhub.api.data.postgres.projection.UserProjectProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 7/26/23
 */
@Repository
@Transactional
public interface ProjectRepo extends JpaRepository<ProjectEntity, String> {
  List<ProjectEntity> findByUserIdOrderByUpdatedAtDesc(String uid);

  @Query(value =
  "SELECT "
  + "p.uid AS projectid,"
  + "p.project_name AS projectname,"
  + "p.description AS description,"
  + "p.updated_at AS updatedat,"
  + "MAX(p.created_at) AS createdat,"
  + "COUNT(pi.uid) AS numfunctions "
  + "FROM public.project p "
  + "LEFT JOIN public.project_item pi ON p.uid = pi.project_id "
  + "WHERE p.user_id = ?1 "
  + "GROUP BY p.uid, p.project_name "
  + "ORDER BY MAX(p.created_at) DESC",
  nativeQuery = true)
  List<UserProjectProjection> findAllUserProjects(String userId);
}
