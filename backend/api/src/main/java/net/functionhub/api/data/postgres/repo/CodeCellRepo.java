package net.functionhub.api.data.postgres.repo;


import java.util.List;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import java.util.UUID;
import net.functionhub.api.data.postgres.projection.Deployment;
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
public interface CodeCellRepo extends JpaRepository<CodeCellEntity, UUID> {
  CodeCellEntity findByUid(UUID uid);

  @Query(value = "SELECT count(*) " +
      "FROM code_cell " +
      "WHERE user_id = ?1",
      nativeQuery = true)
  Integer numActiveCells(String userId);

  CodeCellEntity findBySlug(String slug);

  @Query(value = "SELECT * " +
      "FROM code_cell " +
      "WHERE slug = ?1 AND version = ?2",
      nativeQuery = true)
  CodeCellEntity findBySlugAndVersion(String slug, String version);

  @Query(value = "SELECT cc.* " +
      "FROM public.code_cell cc JOIN public.api_key a ON cc.user_id = a.user_id "
      + "WHERE cc.slug = ?1 AND a.api_key = ?2",
      nativeQuery = true)
  CodeCellEntity findBySlugAndApiKey(String slug, String apiKey);

  @Query(value = "SELECT cc.* "
      + "FROM code_cell cc JOIN project_item pi ON cc.uid = pi.code_id "
      + "WHERE pi.project_id = ?1 "
      + "ORDER BY cc.created_at DESC",
      nativeQuery = true)
  List<CodeCellEntity> findByProjectId(UUID projectId);

}
