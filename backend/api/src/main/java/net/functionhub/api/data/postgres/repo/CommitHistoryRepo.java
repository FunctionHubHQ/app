package net.functionhub.api.data.postgres.repo;


import net.functionhub.api.data.postgres.entity.CommitHistoryEntity;
import net.functionhub.api.data.postgres.projection.Deployment;
import java.util.List;
import java.util.UUID;
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
public interface CommitHistoryRepo extends JpaRepository<CommitHistoryEntity, UUID> {
  List<CommitHistoryEntity> findByCodeCellId(UUID codeCellId);

  @Query(value = "SELECT "
      + "cc.uid as id, "
      + "cc.function_name as name, "
      + "cc.version as version, "
      + "cc.description as description, "
      + "ch.json_schema as payload " +
      "FROM code_cell cc JOIN commit_history ch ON cc.uid = ch.code_cell_id "
      + "WHERE ch.user_id = ?1 AND ch.deployed = true",
      nativeQuery = true)
  List<Deployment> findAllDeployedCommits(String userId);

  @Query(value = "SELECT "
      + "cc.uid as id, "
      + "cc.function_name as name, "
      + "cc.version as version, "
      + "cc.description as description, "
      + "ch.json_schema as payload " +
      "FROM code_cell cc JOIN commit_history ch ON cc.uid = ch.code_cell_id "
      + "WHERE ch.version = ?1 AND ch.deployed = true",
      nativeQuery = true)
  Deployment findDeployedCommitByVersion(String version);

  @Query(value = "SELECT "
      + "cc.uid as id, "
      + "cc.function_name as name, "
      + "cc.version as version, "
      + "cc.description as description, "
      + "ch.json_schema as payload, "
      + "ch.full_openapi_schema as schema " +
      "FROM code_cell cc JOIN commit_history ch ON cc.uid = ch.code_cell_id "
      + "WHERE ch.version = ?1 AND ch.deployed = true AND cc.slug = ?2",
      nativeQuery = true)
  Deployment findDeployedCommitByVersionAndSlug(String version, String slug);

  @Query(value = "SELECT * " +
      "FROM commit_history " +
      "WHERE code_cell_id = ?1 AND version = ?2",
      nativeQuery = true)
  List<CommitHistoryEntity> findByCodeCellIdAndVersion(UUID codeCellId, String version);




}
