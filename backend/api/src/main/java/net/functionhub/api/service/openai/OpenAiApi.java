package net.functionhub.api.service.openai;

import net.functionhub.api.service.openai.completion.CompletionRequest;
import net.functionhub.api.service.openai.edit.EditRequest;
import net.functionhub.api.service.openai.edit.EditResult;
import net.functionhub.api.service.openai.file.File;
import net.functionhub.api.service.openai.image.CreateImageRequest;
import net.functionhub.api.service.openai.image.ImageResult;
import net.functionhub.api.service.openai.moderation.ModerationRequest;
import net.functionhub.api.service.openai.moderation.ModerationResult;
import net.functionhub.api.service.openai.completion.CompletionResult;
import net.functionhub.api.service.openai.embedding.EmbeddingRequest;
import net.functionhub.api.service.openai.embedding.EmbeddingResult;
import net.functionhub.api.service.openai.engine.Engine;
import net.functionhub.api.service.openai.finetune.FineTuneEvent;
import net.functionhub.api.service.openai.finetune.FineTuneRequest;
import net.functionhub.api.service.openai.finetune.FineTuneResult;
import net.functionhub.api.service.openai.model.Model;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.*;

public interface OpenAiApi {

    @GET("v1/models")
    Single<OpenAiResponse<Model>> listModels();

    @GET("/v1/models/{model_id}")
    Single<Model> getModel(@Path("model_id") String modelId);

    @POST("/v1/completions")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request);

    @Deprecated
    @POST("/v1/engines/{engine_id}/completions")
    Single<CompletionResult> createCompletion(@Path("engine_id") String engineId, @Body CompletionRequest request);

    @POST("/v1/edits")
    Single<EditResult> createEdit(@Body EditRequest request);

    @Deprecated
    @POST("/v1/engines/{engine_id}/edits")
    Single<EditResult> createEdit(@Path("engine_id") String engineId, @Body EditRequest request);

    @POST("/v1/embeddings")
    Single<EmbeddingResult> createEmbeddings(@Body EmbeddingRequest request);

    @Deprecated
    @POST("/v1/engines/{engine_id}/embeddings")
    Single<EmbeddingResult> createEmbeddings(@Path("engine_id") String engineId, @Body EmbeddingRequest request);

    @GET("/v1/files")
    Single<OpenAiResponse<File>> listFiles();

    @Multipart
    @POST("/v1/files")
    Single<File> uploadFile(@Part("purpose") RequestBody purpose, @Part MultipartBody.Part file);

    @DELETE("/v1/files/{file_id}")
    Single<DeleteResult> deleteFile(@Path("file_id") String fileId);

    @GET("/v1/files/{file_id}")
    Single<File> retrieveFile(@Path("file_id") String fileId);

    @POST("/v1/fine-tunes")
    Single<FineTuneResult> createFineTune(@Body FineTuneRequest request);

    @POST("/v1/completions")
    Single<CompletionResult> createFineTuneCompletion(@Body CompletionRequest request);

    @GET("/v1/fine-tunes")
    Single<OpenAiResponse<FineTuneResult>> listFineTunes();

    @GET("/v1/fine-tunes/{fine_tune_id}")
    Single<FineTuneResult> retrieveFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("/v1/fine-tunes/{fine_tune_id}/cancel")
    Single<FineTuneResult> cancelFineTune(@Path("fine_tune_id") String fineTuneId);

    @GET("/v1/fine-tunes/{fine_tune_id}/events")
    Single<OpenAiResponse<FineTuneEvent>> listFineTuneEvents(@Path("fine_tune_id") String fineTuneId);

    @DELETE("/v1/models/{fine_tune_id}")
    Single<DeleteResult> deleteFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("/v1/images/generations")
    Single<ImageResult> createImage(@Body CreateImageRequest request);

    @POST("/v1/images/edits")
    Single<ImageResult> createImageEdit(@Body RequestBody requestBody);

    @POST("/v1/images/variations")
    Single<ImageResult> createImageVariation(@Body RequestBody requestBody);

    @POST("/v1/moderations")
    Single<ModerationResult> createModeration(@Body ModerationRequest request);

    @Deprecated
    @GET("v1/engines")
    Single<OpenAiResponse<Engine>> getEngines();

    @Deprecated
    @GET("/v1/engines/{engine_id}")
    Single<Engine> getEngine(@Path("engine_id") String engineId);
}
