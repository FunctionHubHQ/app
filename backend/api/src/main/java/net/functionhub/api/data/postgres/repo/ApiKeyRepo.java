package net.functionhub.api.data.postgres.repo;


import java.util.List;
import net.functionhub.api.data.postgres.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface ApiKeyRepo extends JpaRepository<ApiKeyEntity, Long> {

  List<ApiKeyEntity> findByUserIdOrderByCreatedAtDesc(String userId);

  ApiKeyEntity findByApiKey(String apiKey);

  List<ApiKeyEntity> findByIsVendorKeyAndUserId(boolean isVendorKey, String userId);
}
