package net.functionhub.api.data.postgres.repo;


import net.functionhub.api.data.postgres.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface UserRepo extends JpaRepository<UserEntity, Long> {
    UserEntity findByUid(String uid);

//    @Query(value = "SELECT * " +
//        "FROM user " +
//        "WHERE api_key = ?1",
//        nativeQuery = true)
    UserEntity findByApiKey(String apiKey);
}
