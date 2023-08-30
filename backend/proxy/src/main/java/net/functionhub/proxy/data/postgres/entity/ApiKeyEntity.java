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
 * created on 08/21/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "api_key")
public class ApiKeyEntity {
    @Id
    @SequenceGenerator(name="api_key_sequence_generator",sequenceName="api_key_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="api_key_sequence_generator")
    @Column(name = "id")
    private Long id;
    
    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("api_key")
    @Column(name = "api_key")
    private String apiKey;

    @Basic
    @JsonProperty("is_vendor_key")
    @Column(name = "is_vendor_key")
    private Boolean isVendorKey = false;

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
