package net.functionhub.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "entitlement")
public class EntitlementEntity {
    @Id
    @JsonProperty("uid")
    @Column(name = "uid")
    private UUID uid;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @Basic
    @JsonProperty("max_execution_time")
    @Column(name = "max_execution_time")
    private Long maxExecutionTime = 0L;

    @Basic
    @JsonProperty("max_cpu_time")
    @Column(name = "max_cpu_time")
    private Long maxCpuTime = 0L;

    @Basic
    @JsonProperty("max_memory_usage")
    @Column(name = "max_memory_usage")
    private Long maxMemoryUsage = 0L;

    @Basic
    @JsonProperty("max_data_transfer")
    @Column(name = "max_data_transfer")
    private Long maxDataTransfer = 0L;

    @Basic
    @JsonProperty("max_http_calls")
    @Column(name = "max_http_calls")
    private Long maxHttpCalls = 0L;

    @Basic
    @JsonProperty("max_invocations")
    @Column(name = "max_invocations")
    private Long maxInvocations = 0L;

    @Basic
    @JsonProperty("max_functions")
    @Column(name = "max_functions")
    private Long maxFunctions = 0L;

    @Basic
    @JsonProperty("max_projects")
    @Column(name = "max_projects")
    private Long maxProjects = 0L;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
