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
    UserEntity findByEmail(String email);

    @Query(value = "SELECT "
        + "u.uid as uid, "
        + "u.email as email, "
        + "u.full_name as name, "
        + "u.avatar_url as avatar, "
        + "u.username as username, "
        + "a.api_key as apikey, "
        + "e.max_execution_time as maxexecutiontime, "
        + "e.max_cpu_time as maxcputime, "
        + "e.max_memory_usage as maxmemoryusage, "
        + "e.max_data_transfer as maxdatatransfer, "
        + "e.max_http_calls as maxhttpcalls,"
        + " e.max_invocations as maxinvocations, "
        + "e.max_functions as maxfunctions, "
        + "e.max_projects as maxprojects  " +
        "FROM public.user u "
        + "JOIN public.api_key a ON u.uid = a.user_id "
        + "JOIN public.entitlement e ON u.uid = e.user_id "
        + "WHERE a.api_key = ?1",
        nativeQuery = true)
    UserProjection findByApiKey(String apiKey);


    @Query(value = "SELECT "
        + "u.uid as uid, "
        + "u.email as email, "
        + "u.full_name as name, "
        + "u.avatar_url as avatar, "
        + "u.username as username, "
        + "e.max_execution_time as maxexecutiontime, "
        + "e.max_cpu_time as maxcputime, "
        + "e.max_memory_usage as maxmemoryusage, "
        + "e.max_data_transfer as maxdatatransfer, "
        + "e.max_http_calls as maxhttpcalls,"
        + "e.max_invocations as maxinvocations, "
        + "e.max_functions as maxfunctions, "
        + "e.max_projects as maxprojects  " +
        "FROM public.user u "
        + "JOIN public.entitlement e ON u.uid = e.user_id "
        + "WHERE u.uid = ?1",
        nativeQuery = true)
    UserProjection findProjectionByUid(String uid);

    @Query(value = "SELECT count(*)" +
        "FROM public.user u "
        + "WHERE u.username = ?1",
        nativeQuery = true)
    Integer findUsernameCount(String username);
}
