package net.functionhub.api.service.openai.completion;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.functionhub.api.dto.GPTFunctionCall;
import lombok.Data;

/**
 * @author Biz Melesse created on 6/13/23
 */
@Data
public class CompletionResponseMessage {
  private String role;
  private String content;

  @JsonProperty("function_call")
  GPTFunctionCall functionCall;
}
