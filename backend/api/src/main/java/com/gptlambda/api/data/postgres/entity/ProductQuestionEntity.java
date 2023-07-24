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
@Table(schema = "public", name = "product_question")
public class ProductQuestionEntity {
    @Id
    @SequenceGenerator(name="product_question_sequence_generator",sequenceName="product_question_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO, generator="product_question_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @JsonProperty("question_id")
    @Column(name = "question_id")
    private String questionId;

    @Basic
    @JsonProperty("num_answers")
    @Column(name = "num_answers")
    private Long numAnswers;

    @Basic
    @JsonProperty("upvotes")
    @Column(name = "upvotes")
    private Long upvotes;

    @Basic
    @Column(name = "question")
    private String question;

    @Basic
    @JsonProperty("has_embedding")
    @Column(name = "has_embedding")
    private Boolean hasEmbedding = false;

    @Basic
    @JsonProperty("asked_at")
    @Column(name = "asked_at")
    private LocalDateTime askedAt;

    @Basic
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
