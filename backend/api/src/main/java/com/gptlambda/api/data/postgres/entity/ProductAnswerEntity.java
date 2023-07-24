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
 * created on 6/1/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "product_answer")
public class ProductAnswerEntity {
    @Id
    @SequenceGenerator(name="product_answer_sequence_generator",sequenceName="product_answer_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO, generator="product_answer_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @JsonProperty("product_question_id")
    @Column(name = "product_question_id")
    private Long productQuestionId;

    @Basic
    @JsonProperty("answer_id")
    @Column(name = "answer_id")
    private String answerId;

    @Basic
    @Column(name = "answer")
    private String answer;

    @Basic
    @JsonProperty("answered_by")
    @Column(name = "answered_by")
    private String answeredBy;

    @Basic
    @Column(name = "upvotes")
    private Long upvotes = 0L;

    @Basic
    @Column(name = "downvotes")
    private Long downvotes = 0L;

    @Basic
    @JsonProperty("has_embedding")
    @Column(name = "has_embedding")
    private Boolean hasEmbedding = false;

    @Basic
    @JsonProperty("answered_at")
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Basic
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
