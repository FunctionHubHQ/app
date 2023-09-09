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
@Table(schema = "public", name = "project")
public class ProjectEntity {
    @Id
    @JsonProperty("id")
    @Column(name = "id")
    private String id;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("project_name")
    @Column(name = "project_name")
    private String projectName;

    @Basic
    @JsonProperty("description")
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
