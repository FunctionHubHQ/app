package com.gptlambda.api.service.openai;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.service.ServiceTestConfiguration;
import com.gptlambda.api.service.openai.finetune.FineTuneEvent;
import com.gptlambda.api.service.openai.finetune.FineTuneRequest;
import com.gptlambda.api.service.openai.finetune.FineTuneResult;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class FineTuneTest extends AbstractTestNGSpringContextTests {
    private OpenAiService service;
    private String fileId;
    private String fineTuneId;
    @Autowired
    private OpenAiProps openAiProps;


    @BeforeClass
    public void setup() throws InterruptedException {
      service = new OpenAiService(openAiProps.getApiKey());
      fileId = service.uploadFile("fine-tune", "src/test/resources/openai/fine-tuning-data.jsonl").getId();

      // wait for file to be processed
      TimeUnit.SECONDS.sleep(10);
    }


    @AfterClass
    void teardown() {
        service.deleteFile(fileId);
    }

    @Test
    void createFineTune() {
        FineTuneRequest request = FineTuneRequest.builder()
                .trainingFile(fileId)
                .model("ada")
                .build();

        FineTuneResult fineTune = service.createFineTune(request);
        fineTuneId = fineTune.getId();

        assertEquals("pending", fineTune.getStatus());
    }

    @Test(dependsOnMethods = {"createFineTune"})
    void listFineTunes() {
        List<FineTuneResult> fineTunes = service.listFineTunes();

        assertTrue(fineTunes.stream().anyMatch(fineTune -> fineTune.getId().equals(fineTuneId)));
    }

    @Test(dependsOnMethods = {"listFineTunes"})
    void listFineTuneEvents() {
        List<FineTuneEvent> events = service.listFineTuneEvents(fineTuneId);

        assertFalse(events.isEmpty());
    }

    @Test(dependsOnMethods = {"listFineTuneEvents"})
    void retrieveFineTune() {
        FineTuneResult fineTune = service.retrieveFineTune(fineTuneId);

        assertEquals("ada", fineTune.getModel());
    }

    @Test(dependsOnMethods = {"retrieveFineTune"})
    void cancelFineTune() {
        FineTuneResult fineTune = service.cancelFineTune(fineTuneId);

        assertEquals("cancelled", fineTune.getStatus());
    }
}
