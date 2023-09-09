package net.functionhub.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(schema = "public", name = "commit_history")
public class CommitHistoryEntity {
    @Id
    @JsonProperty("id")
    @Column(name = "id")
    private String id;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("code_cell_id")
    @Column(name = "code_cell_id")
    private String codeCellId;

    @JsonProperty("version")
    @Column(name = "version")
    private String version;

    @JsonProperty("code")
    @Column(name = "code")
    private String code;

    @Basic
    @JsonProperty("json_schema")
    @Column(name = "json_schema")
    private String jsonSchema;

    @Basic
    @JsonProperty("full_openapi_schema")
    @Column(name = "full_openapi_schema")
    private String fullOpenApiSchema;

    @Basic
    @JsonProperty("deployed")
    @Column(name = "deployed")
    private Boolean deployed = false;

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
