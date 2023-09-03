package net.functionhub.api.dto;

import lombok.Data;

/**
 * @author Biz Melesse created on 9/2/23
 */
@Data
public class Entitlement {
  private Long maxExecutionTime;
  private Long maxCpuTime;
  private Long maxMemoryUsage;
  private Long maxBandwidth;
  private Long numHttpCalls;
  private Long numInvocations;
  private Long numFunctions;
  private Long numProjects;
}
