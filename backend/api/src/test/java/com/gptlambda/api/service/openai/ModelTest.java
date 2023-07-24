package com.gptlambda.api.service.openai;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.service.ServiceTestConfiguration;
import com.gptlambda.api.service.openai.model.Model;
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
public class ModelTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private OpenAiProps openAiProps;
    private OpenAiService service;

    @BeforeClass
    public void setup() {
        service = new OpenAiService(openAiProps.getApiKey());
    }


    @Test
    void listModels() {
        List<Model> models = service.listModels();

        assertFalse(models.isEmpty());
    }

    @Test
    void getModel() {
        Model ada = service.getModel("ada");

        assertEquals("ada", ada.id);
        assertEquals("openai", ada.ownedBy);
        assertFalse(ada.permission.isEmpty());
    }
}
