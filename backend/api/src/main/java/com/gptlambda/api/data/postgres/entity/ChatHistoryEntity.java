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
@Table(schema = "public", name = "chat_history")
public class ChatHistoryEntity {
    @Id
    @SequenceGenerator(name="chat_history_sequence_generator",sequenceName="chat_history_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="chat_history_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("fcm_token_id")
    @Column(name = "fcm_token_id")
    private Long fcmTokenId;

    @Basic
    @JsonProperty("chat_id")
    @Column(name = "chat_id")
    private String chatId;

    @Basic
    @JsonProperty("product_sku")
    @Column(name = "product_sku")
    private String productSku;

    @Basic
    @JsonProperty("message")
    @Column(name = "message")
    private String message;

    @Basic
    @JsonProperty("is_gpt")
    @Column(name = "is_gpt")
    private Boolean isGpt;


    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
