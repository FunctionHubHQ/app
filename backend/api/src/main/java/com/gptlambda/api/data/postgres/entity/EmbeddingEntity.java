package com.gptlambda.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.json.JsonType;
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
import org.hibernate.annotations.Type;

/**
 * @author Bizuwork Melesse
 * created on 5/25/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "embedding")
public class EmbeddingEntity {
    @Id
    @SequenceGenerator(name="embedding_sequence_generator",sequenceName="embedding_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="embedding_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @JsonProperty("review_id")
    @Column(name = "review_id")
    private Long reviewId;

    @Basic
    @JsonProperty("question_id")
    @Column(name = "question_id")
    private Long questionId;

    @Basic
    @JsonProperty("answer_id")
    @Column(name = "answer_id")
    private Long answerId;

    @Type(JsonType.class)
    @Column(name = "embedding")
    private Double[] embedding;

    @Basic
    @Column(name = "text")
    private String text;

    @Basic
    @JsonProperty("product_sku")
    @Column(name = "product_sku")
    private String productSku;

    @Basic
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
