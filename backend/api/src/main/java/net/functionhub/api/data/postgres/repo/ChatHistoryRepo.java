package net.functionhub.api.data.postgres.repo;


import net.functionhub.api.data.postgres.entity.ChatHistoryEntity;
import java.util.List;
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
public interface ChatHistoryRepo extends JpaRepository<ChatHistoryEntity, Long> {

  @Query(value = "SELECT * " +
      "FROM chat_history " +
      "WHERE fcm_token_id = ?1 AND product_sku = ?2 "
      + "ORDER BY created_at DESC "
      + "LIMIT ?3",
      nativeQuery = true)
  List<ChatHistoryEntity> findLastNMessages(Long fcmTokenId, String productSku, Long n);
}
