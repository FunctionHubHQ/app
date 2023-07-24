package com.gptlambda.api.service.openai;

import static java.time.Duration.ofSeconds;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.gptlambda.api.service.openai.completion.CompletionRequest;
import com.gptlambda.api.service.openai.completion.CompletionResult;
import com.gptlambda.api.service.openai.edit.EditRequest;
import com.gptlambda.api.service.openai.edit.EditResult;
import com.gptlambda.api.service.openai.file.File;
import com.gptlambda.api.service.openai.image.CreateImageEditRequest;
import com.gptlambda.api.service.openai.image.CreateImageRequest;
import com.gptlambda.api.service.openai.image.CreateImageVariationRequest;
import com.gptlambda.api.service.openai.image.ImageResult;
import com.gptlambda.api.service.openai.moderation.ModerationRequest;
import com.gptlambda.api.service.openai.moderation.ModerationResult;
import com.gptlambda.api.service.openai.embedding.EmbeddingRequest;
import com.gptlambda.api.service.openai.embedding.EmbeddingResult;
import com.gptlambda.api.service.openai.engine.Engine;
import com.gptlambda.api.service.openai.finetune.FineTuneEvent;
import com.gptlambda.api.service.openai.finetune.FineTuneRequest;
import com.gptlambda.api.service.openai.finetune.FineTuneResult;
import com.gptlambda.api.service.openai.image.*;
import com.gptlambda.api.service.openai.model.Model;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class OpenAiService {

    private static final String BASE_URL = "https://api.openai.com/";

    final OpenAiApi api;

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     */
    public OpenAiService(final String token) {
        this(token, BASE_URL, ofSeconds(10));
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout in seconds, 0 means no timeout
     * @deprecated use {@link OpenAiService(String, Duration)}
     */
    @Deprecated
    public OpenAiService(final String token, final int timeout) {
        this(token, BASE_URL, ofSeconds(timeout));
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout, Duration.ZERO means no timeout
     */
    public OpenAiService(final String token, final Duration timeout) {
        this(token, BASE_URL, timeout);
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout, Duration.ZERO means no timeout
     */
    public OpenAiService(final String token, final String baseUrl, final Duration timeout) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthenticationInterceptor(token))
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        this.api = retrofit.create(OpenAiApi.class);
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param api OpenAiApi instance to use for all methods
     */
    public OpenAiService(final OpenAiApi api) {
        this.api = api;
    }

    public List<Model> listModels() {
        return api.listModels().blockingGet().data;
    }

    public Model getModel(String modelId) {
        return api.getModel(modelId).blockingGet();
    }

    public CompletionResult createCompletion(CompletionRequest request) {
        return api.createCompletion(request).blockingGet();
    }

    /**
     * Use {@link OpenAiService#createCompletion(CompletionRequest)} and {@link CompletionRequest#model}instead
     */
    @Deprecated
    public CompletionResult createCompletion(String engineId, CompletionRequest request) {
        return api.createCompletion(engineId, request).blockingGet();
    }

    public EditResult createEdit(EditRequest request) {
        return api.createEdit(request).blockingGet();
    }

    /**
     * Use {@link OpenAiService#createEdit(EditRequest)} and {@link EditRequest#model}instead
     */
    @Deprecated
    public EditResult createEdit(String engineId, EditRequest request) {
        return api.createEdit(engineId, request).blockingGet();
    }

    public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
        return api.createEmbeddings(request).blockingGet();
    }

    /**
     * Use {@link OpenAiService#createEmbeddings(EmbeddingRequest)} and {@link EmbeddingRequest#model}instead
     */
    @Deprecated
    public EmbeddingResult createEmbeddings(String engineId, EmbeddingRequest request) {
        return api.createEmbeddings(engineId, request).blockingGet();
    }

    public List<File> listFiles() {
        return api.listFiles().blockingGet().data;
    }

    public File uploadFile(String purpose, String filepath) {
        java.io.File file = new java.io.File(filepath);
        RequestBody purposeBody = RequestBody.create(MultipartBody.FORM, purpose);
        RequestBody fileBody = RequestBody.create(MediaType.parse("text"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filepath, fileBody);

        return api.uploadFile(purposeBody, body).blockingGet();
    }

    public DeleteResult deleteFile(String fileId) {
        return api.deleteFile(fileId).blockingGet();
    }

    public File retrieveFile(String fileId) {
        return api.retrieveFile(fileId).blockingGet();
    }

    public FineTuneResult createFineTune(FineTuneRequest request) {
        return api.createFineTune(request).blockingGet();
    }

    public CompletionResult createFineTuneCompletion(CompletionRequest request) {
        return api.createFineTuneCompletion(request).blockingGet();
    }

    public List<FineTuneResult> listFineTunes() {
        return api.listFineTunes().blockingGet().data;
    }

    public FineTuneResult retrieveFineTune(String fineTuneId) {
        return api.retrieveFineTune(fineTuneId).blockingGet();
    }

    public FineTuneResult cancelFineTune(String fineTuneId) {
        return api.cancelFineTune(fineTuneId).blockingGet();
    }

    public List<FineTuneEvent> listFineTuneEvents(String fineTuneId) {
        return api.listFineTuneEvents(fineTuneId).blockingGet().data;
    }

    public DeleteResult deleteFineTune(String fineTuneId) {
        return api.deleteFineTune(fineTuneId).blockingGet();
    }

    public ImageResult createImage(CreateImageRequest request) {
        return api.createImage(request).blockingGet();
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, String imagePath, String maskPath) {
        java.io.File image = new java.io.File(imagePath);
        java.io.File mask = null;
        if (maskPath != null) {
            mask = new java.io.File(maskPath);
        }
        return createImageEdit(request, image, mask);
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, java.io.File image, java.io.File mask) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("prompt", request.getPrompt())
                .addFormDataPart("size", request.getSize())
                .addFormDataPart("response_format", request.getResponseFormat())
                .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        if (mask != null) {
            RequestBody maskBody = RequestBody.create(MediaType.parse("image"), mask);
            builder.addFormDataPart("mask", "mask", maskBody);
        }

        return api.createImageEdit(builder.build()).blockingGet();
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, String imagePath) {
        java.io.File image = new java.io.File(imagePath);
        return createImageVariation(request, image);
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, java.io.File image) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("size", request.getSize())
                .addFormDataPart("response_format", request.getResponseFormat())
                .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        return api.createImageVariation(builder.build()).blockingGet();
    }

    public ModerationResult createModeration(ModerationRequest request) {
        return api.createModeration(request).blockingGet();
    }

    @Deprecated
    public List<Engine> getEngines() {
        return api.getEngines().blockingGet().data;
    }

    @Deprecated
    public Engine getEngine(String engineId) {
        return api.getEngine(engineId).blockingGet();
    }
}
