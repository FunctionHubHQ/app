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
@Table(schema = "public", name = "review")
public class ReviewEntity {
    @Id
    @SequenceGenerator(name="review_sequence_generator",sequenceName="review_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="review_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @Column(name = "title")
    private String title;

    @Basic
    @JsonProperty("review_id")
    @Column(name = "review_id")
    private String reviewId;

    @Basic
    @Column(name = "merchant")
    private String merchant;

    @Basic
    @Column(name = "country")
    private String country;

    @Basic
    @JsonProperty("verified_purchase")
    @Column(name = "verified_purchase")
    private Boolean verifiedPurchase = false;

    @Basic
    @Column(name = "upvotes")
    private Long upvotes;

    @Basic
    @JsonProperty("star_rating")
    @Column(name = "star_rating")
    private Integer starRating;

    @Basic
    @Column(name = "reviewer")
    private String reviewer;

    @Basic
    @Column(name = "body")
    private String body;

    @Basic
    @JsonProperty("has_embedding")
    @Column(name = "has_embedding")
    private Boolean hasEmbedding = false;

    @Basic
    @JsonProperty("submitted_at")
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Basic
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
