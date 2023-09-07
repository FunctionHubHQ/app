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
 * created on 8/17/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "project_item")
public class ProjectItemEntity {
    @Id
    @JsonProperty("uid")
    @Column(name = "uid")
    private String uid;

    @JsonProperty("code_id")
    @Column(name = "code_id")
    private String codeId;

    @JsonProperty("project_id")
    @Column(name = "project_id")
    private String projectId;

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
