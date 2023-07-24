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
@Table(schema = "public", name = "task")
public class TaskEntity {
    @Id
    @SequenceGenerator(name="task_sequence_generator",sequenceName="task_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="task_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @JsonProperty("task_id")
    @Column(name = "task_id")
    private String taskId;

    @Basic
    @JsonProperty("question_id")
    @Column(name = "question_id")
    private String questionId;

    @Basic
    @JsonProperty("star_rating")
    @Column(name = "star_rating")
    private String starRating;

    @Basic
    @JsonProperty("job_type")
    @Column(name = "job_type")
    private String jobType;

    @Basic
    @Column(name = "url")
    private String url;

    @Basic
    @Column(name = "completed")
    private Boolean completed = false;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
