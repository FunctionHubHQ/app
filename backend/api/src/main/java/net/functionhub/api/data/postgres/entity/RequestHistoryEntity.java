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
 * created on 2/13/21
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "request_history")
public class RequestHistoryEntity {
    @Id
    @JsonProperty("id")
    @Column(name = "id")
    private String id;
    
    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("http_method")
    @Column(name = "http_method")
    private String httpMethod;

    @JsonProperty("execution_id")
    @Column(name = "execution_id")
    private String executionId;

    @JsonProperty("url")
    @Column(name = "url")
    private String url;

    @Basic
    @JsonProperty("error_message")
    @Column(name = "error_message")
    private String errorMessage;

    @Basic
    @JsonProperty("request_ended_at")
    @Column(name = "request_ended_at")
    private LocalDateTime requestEndedAt;

    @Basic
    @JsonProperty("request_started_at")
    @Column(name = "request_started_at")
    private LocalDateTime requestStartedAt;

    @Basic
    @JsonProperty("request_duration")
    @Column(name = "request_duration")
    private Integer requestDuration;

    @Basic
    @JsonProperty("http_status_code")
    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Basic
    @JsonProperty("request_content_length")
    @Column(name = "request_content_length")
    private Integer requestContentLength;

    @Basic
    @JsonProperty("response_content_length")
    @Column(name = "response_content_length")
    private Integer responseContentLength;

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
