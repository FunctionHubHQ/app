package com.gptlambda.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse
 * created on 11/16/22
 */
@Getter
@Setter
public class ScrapedContentDto {
  private String title;
  private String description;
  private String canonicalUrl;
  private String creatorHandle;
}
