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
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "entitlement")
public class EntitlementEntity {
    @Id
    @JsonProperty("uid")
    @Column(name = "uid")
    private UUID uid;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @Basic
    @JsonProperty("timeout")
    @Column(name = "timeout")
    private Long timeout;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
