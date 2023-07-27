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
@Table(schema = "public", name = "code_cell")
public class CodeCellEntity {
    @Id
    @JsonProperty("uid")
    @Column(name = "uid")
    private UUID uid;

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
    @JsonProperty("request_dto")
    @Column(name = "request_dto")
    private String requestDto;

    @Basic
    @JsonProperty("response_dto")
    @Column(name = "response_dto")
    private String responseDto;

    @Basic
    @JsonProperty("version")
    @Column(name = "version")
    private String version;

    @Basic
    @JsonProperty("slug")
    @Column(name = "slug")
    private String slug;

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
