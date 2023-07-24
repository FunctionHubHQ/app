package com.gptlambda.api.service.openai;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.gptlambda.api.props.OpenAiProps;
import com.gptlambda.api.service.openai.image.CreateImageEditRequest;
import com.gptlambda.api.service.openai.image.CreateImageRequest;
import com.gptlambda.api.service.openai.image.CreateImageVariationRequest;
import com.gptlambda.api.service.openai.image.Image;
import com.gptlambda.api.service.ServiceTestConfiguration;
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
public class ImageTest extends AbstractTestNGSpringContextTests {

    static String filePath = "src/test/resources/openai/penguin.png";
    static String fileWithAlphaPath = "src/test/resources/openai/penguin_with_alpha.png";
    static String maskPath = "src/test/resources/openai/mask.png";

    @Autowired
    private OpenAiProps openAiProps;
    private OpenAiService service;

    @BeforeClass
    public void setup() {
        service = new OpenAiService(openAiProps.getApiKey(), 30);
    }


    @Test
    void createImageUrl() {
        CreateImageRequest createImageRequest = CreateImageRequest.builder()
                .prompt("penguin")
                .n(3)
                .size("256x256")
                .user("testing")
                .build();

        List<Image> images = service.createImage(createImageRequest).getData();
        assertEquals(3, images.size());
        assertNotNull(images.get(0).getUrl());
    }

    @Test
    void createImageBase64() {
        CreateImageRequest createImageRequest = CreateImageRequest.builder()
                .prompt("penguin")
                .responseFormat("b64_json")
                .user("testing")
                .build();

        List<Image> images = service.createImage(createImageRequest).getData();
        assertEquals(1, images.size());
        assertNotNull(images.get(0).getB64Json());
    }

    @Test
    void createImageEdit() {
        CreateImageEditRequest createImageRequest = CreateImageEditRequest.builder()
                .prompt("a penguin with a red background")
                .responseFormat("url")
                .size("256x256")
                .user("testing")
                .n(2)
                .build();

        List<Image> images = service.createImageEdit(createImageRequest, fileWithAlphaPath, null).getData();
        assertEquals(2, images.size());
        assertNotNull(images.get(0).getUrl());
    }

    @Test
    void createImageEditWithMask() {
        CreateImageEditRequest createImageRequest = CreateImageEditRequest.builder()
                .prompt("a penguin with a red hat")
                .responseFormat("url")
                .size("256x256")
                .user("testing")
                .n(2)
                .build();

        List<Image> images = service.createImageEdit(createImageRequest, filePath, maskPath).getData();
        assertEquals(2, images.size());
        assertNotNull(images.get(0).getUrl());
    }

    @Test
    void createImageVariation() {
        CreateImageVariationRequest createImageVariationRequest = CreateImageVariationRequest.builder()
                .responseFormat("url")
                .size("256x256")
                .user("testing")
                .n(2)
                .build();

        List<Image> images = service.createImageVariation(createImageVariationRequest, filePath).getData();
        assertEquals(2, images.size());
        assertNotNull(images.get(0).getUrl());
    }
}
