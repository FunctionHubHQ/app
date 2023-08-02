package com.gptlambda.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(schema = "public", name = "code_cell")
public class CodeCellEntity {
    @Id
    @JsonProperty("uid")
    @Column(name = "uid")
    private UUID uid;

    @JsonProperty("parent_id")
    @Column(name = "parent_id")
    private UUID parentId;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("function_name")
    @Column(name = "function_name")
    private String functionName;

    @Basic
    @JsonProperty("description")
    @Column(name = "description")
    private String description;

    @Basic
    @JsonProperty("code")
    @Column(name = "code")
    private String code;

    @Basic
    @JsonProperty("interfaces")
    @Column(name = "interfaces")
    private String interfaces;

    @Basic
    @JsonProperty("json_schema")
    @Column(name = "json_schema")
    private String jsonSchema;


    @Basic
    @JsonProperty("version")
    @Column(name = "version")
    private String version;

    @Basic
    @JsonProperty("deployed_version")
    @Column(name = "deployed_version")
    private String deployedVersion;

    @Basic
    @JsonProperty("slug")
    @Column(name = "slug")
    private String slug;

    @Basic
    @JsonProperty("reason_not_deployable")
    @Column(name = "reason_not_deployable")
    private String reasonNotDeployable;

    @Basic
    @JsonProperty("deployed")
    @Column(name = "deployed")
    private Boolean deployed = false;

    @Basic
    @JsonProperty("is_deployable")
    @Column(name = "is_deployable")
    private Boolean isDeployable = true;

    @Basic
    @JsonProperty("is_active")
    @Column(name = "is_active")
    private Boolean isActive = false;

    @Basic
    @JsonProperty("is_public")
    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
