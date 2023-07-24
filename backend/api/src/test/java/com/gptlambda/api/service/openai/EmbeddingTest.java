package com.gptlambda.api.service.openai;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.service.ServiceTestConfiguration;
import com.gptlambda.api.service.openai.embedding.Embedding;
import com.gptlambda.api.service.openai.embedding.EmbeddingRequest;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class EmbeddingTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private OpenAiProps openAiProps;
    private OpenAiService service;

    @BeforeClass
    public void setup() {
        service = new OpenAiService(openAiProps.getApiKey());
    }

    @Test
    void createEmbeddings() {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-similarity-babbage-001")
                .input(Collections.singletonList("The food was delicious and the waiter..."))
                .build();

        List<Embedding> embeddings = service.createEmbeddings(embeddingRequest).getData();

        assertFalse(embeddings.isEmpty());
        assertFalse(embeddings.get(0).getEmbedding().isEmpty());
    }

    @Test
    void createEmbeddingsDeprecated() {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .input(Collections.singletonList("The food was delicious and the waiter..."))
                .build();

        List<Embedding> embeddings = service.createEmbeddings("text-similarity-babbage-001", embeddingRequest).getData();

        assertFalse(embeddings.isEmpty());
        assertFalse(embeddings.get(0).getEmbedding().isEmpty());
    }
}
