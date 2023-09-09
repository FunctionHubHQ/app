package net.functionhub.api.data.postgres.entity;

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
 * created on 08/21/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "api_key")
public class ApiKeyEntity {
    @Id
    @JsonProperty("id")
    @Column(name = "id")
    private String id;
    
    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("api_key")
    @Column(name = "api_key")
    private String apiKey;

    @Basic
    @JsonProperty("provider")
    @Column(name = "provider")
    private String provider;

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
