package net.functionhub.proxy.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "user")
public class UserEntity {
    @Id
    @SequenceGenerator(name="user_sequence_generator",sequenceName="user_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="user_sequence_generator")
    @Column(name = "id")
    private Long id;
    
    @JsonProperty("uid")
    @Column(name = "uid")
    private String uid;

    @JsonProperty("username")
    @Column(name = "username")
    private String username;

    @JsonProperty("avatar_url")
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Basic
    @JsonProperty("full_name")
    @Column(name = "full_name")
    private String fullName;

    @Basic
    @JsonProperty("is_premium_user")
    @Column(name = "is_premium_user")
    private Boolean isPremiumUser = false;

    @Basic
    @JsonProperty("anonymous")
    @Column(name = "anonymous")
    private Boolean anonymous = false;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
