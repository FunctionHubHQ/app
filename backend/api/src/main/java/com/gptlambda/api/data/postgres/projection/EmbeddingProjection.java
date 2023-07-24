package com.gptlambda.api.data.postgres.projection;

/**
 * @author Biz Melesse created on 6/13/23
 */
public interface EmbeddingProjection {
  Long getId();
  String getContent();
  float getSimilarity();

}
