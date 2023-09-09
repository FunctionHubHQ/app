package net.functionhub.api.data.postgres.repo;


import java.util.List;
import net.functionhub.api.data.postgres.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface ApiKeyRepo extends JpaRepository<ApiKeyEntity, String> {
  ApiKeyEntity findByApiKey(String apiKey);

  @Query(value = "SELECT * F"
      + "ROM public.api_key "
      + "WHERE user_id = ?1 "
      + "ORDER BY created_at ASC LIMIT 1",
      nativeQuery = true)
  ApiKeyEntity findOldestApiKey(String userId);

  @Query(value = "SELECT * "
      + "FROM public.api_key "
      + "WHERE provider = ?1 AND user_id = ?2 "
      + "ORDER BY created_at DESC",
      nativeQuery = true)
  List<ApiKeyEntity> findAllByProvider(String provider, String userId);

  @Query(value = "SELECT * "
      + "FROM public.api_key "
      + "WHERE provider IN (?1) AND user_id = ?2 "
      + "ORDER BY created_at DESC",
      nativeQuery = true)
  List<ApiKeyEntity> findAllByProviders(List<String> providers, String userId);
}
