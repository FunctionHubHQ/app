package com.gptlambda.api.data.postgres.entity;

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
 * created on 5/25/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "fcm_token")
public class FcmTokenEntity {
    @Id
    @SequenceGenerator(name="fcm_token_sequence_generator",sequenceName="fcm_token_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="fcm_token_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("user_id")
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @JsonProperty("fcm_token")
    @Column(name = "fcm_token")
    private String fcmToken;


    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
