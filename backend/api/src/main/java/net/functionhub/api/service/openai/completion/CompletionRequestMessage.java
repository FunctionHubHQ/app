package net.functionhub.api.service.openai.completion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Biz Melesse created on 6/13/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletionRequestMessage {
  private String role;
  private String content;
}
