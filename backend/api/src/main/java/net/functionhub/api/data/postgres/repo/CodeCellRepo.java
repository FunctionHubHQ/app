package net.functionhub.api.data.postgres.repo;


import net.functionhub.api.data.postgres.entity.CodeCellEntity;
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
public interface CodeCellRepo extends JpaRepository<CodeCellEntity, UUID> {
  CodeCellEntity findByUid(UUID uid);

  @Query(value = "SELECT count(*) " +
      "FROM code_cell " +
      "WHERE user_id = ?1",
      nativeQuery = true)
  Integer numActiveCells(String userId);

  CodeCellEntity findBySlug(String slug);


}
