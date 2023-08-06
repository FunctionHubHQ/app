package net.functionhub.api.service.openai.completion;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A request for OpenAi to generate a predicted completion for a prompt.
 * All fields are nullable.
 *
 * https://beta.openai.com/docs/api-reference/completions/create
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionRequestFunctionalCall {

    /**
     * The name of the model to use.
     * Required if specifying a fine-tuned model or if using the new v1/completions endpoint.
     */
    String model;

    /**
     * The maximum number of tokens to generate.
     * Requests can use up to 2048 tokens shared between prompt and completion.
     * (One token is roughly 4 characters for normal English text)
     */
    @JsonProperty("max_tokens")
    Integer maxTokens;

    /**
     * What sampling temperature to use. Higher values means the model will take more risks.
     * Try 0.9 for more creative applications, and 0 (argmax sampling) for ones with a well-defined answer.
     */
    Double temperature;

    /**
     * A unique identifier representing your end-user, which will help OpenAI to monitor and detect abuse.
     */
    String user;


    /**
     * A list of messages can be provided in chatbot-like conversation setting
     * From:
     *   openai.ChatCompletion.create(
     *     model="gpt-3.5-turbo",
     *     messages=[
     *         {"role": "system", "content": "You are a helpful assistant."},
     *         {"role": "user", "content": "Who won the world series in 2020?"},
     *         {"role": "assistant", "content": "The Los Angeles Dodgers won the World Series in 2020."},
     *         {"role": "user", "content": "Where was it played?"}
     *     ])
     */
    List<Map<String, Object>> messages;
}
