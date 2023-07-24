package com.gptlambda.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 5/25/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "feedback")
public class FeedbackEntity {
    @Id
    @SequenceGenerator(name="feedback_sequence_generator",sequenceName="feedback_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="feedback_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("subject")
    @Column(name = "subject")
    private String subject;

    @Basic
    @JsonProperty("body")
    @Column(name = "body")
    private String body;

    @Basic
    @JsonProperty("user_id")
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
