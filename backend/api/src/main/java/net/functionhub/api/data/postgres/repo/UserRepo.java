package net.functionhub.api.data.postgres.repo;


import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.projection.UserProjection;
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
public interface UserRepo extends JpaRepository<UserEntity, Long> {
    UserEntity findByUid(String uid);

    @Query(value = "SELECT "
        + "u.uid as uid, "
        + "u.email as email, "
        + "u.full_name as name, "
        + "u.avatar_url as avatar, "
        + "a.api_key as api_key " +
        "FROM public.user u JOIN public.api_key a ON u.uid = a.user_id "
        + "WHERE a.api_key = ?1",
        nativeQuery = true)
    UserProjection findByApiKey(String apiKey);

    @Query(value = "SELECT "
        + "u.uid as uid, "
        + "u.email as email, "
        + "u.full_name as name, "
        + "u.avatar_url as avatar, "
        + "a.api_key as api_key " +
        "FROM public.user u JOIN public.api_key a ON u.uid = a.user_id "
        + "WHERE u.uid = ?1",
        nativeQuery = true)
    UserProjection findProjectionByUid(String uid);
}
