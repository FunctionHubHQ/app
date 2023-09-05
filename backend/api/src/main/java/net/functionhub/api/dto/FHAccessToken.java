package net.functionhub.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 9/4/23
 */
@Getter @Setter
public class FHAccessToken {
  /**
   * Bandwidth or data transfer limit per HTTP call
   */
  private Long dtl;

  /**
   * Request ID (execution ID alias)
   */
  private String rid;

  /**
   * Function Hub user ID
   */
  private String uid;


  /**
   * Number of HTTP calls per execution
   */
  private Long hce;
}
