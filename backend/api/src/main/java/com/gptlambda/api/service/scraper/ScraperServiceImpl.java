package com.gptlambda.api.service.scraper;

import static com.amazonaws.util.StringUtils.UTF8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gptlambda.api.data.postgres.entity.ProductEntity;
import com.gptlambda.api.data.postgres.entity.TaskEntity;
import com.gptlambda.api.data.postgres.repo.ProductRepo;
import com.gptlambda.api.data.postgres.repo.TaskRepo;
import com.gptlambda.api.dto.JobType;
import com.gptlambda.api.dto.ScrapingJob;
import com.gptlambda.api.dto.SmartProxyJob;
import com.gptlambda.api.dto.SmartProxyResultsDto;
import com.gptlambda.api.dto.SmartProxyTaskDto;
import com.gptlambda.api.props.InfaticaProps;
import com.gptlambda.api.props.RabbitMQProps;
import com.gptlambda.api.props.ScraperProps;
import com.gptlambda.api.props.SmartProxyProps;
import com.gptlambda.api.service.parser.AmazonParser;
import com.gptlambda.api.service.utils.JobSemaphore;
import com.gptlambda.api.service.utils.JobUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Bizuwork Melesse
 * created on 6/17/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperServiceImpl implements ScraperService {
    private final AmazonParser amazonParser;
    private final ScraperUtils scraperUtils;
    private final JobSemaphore jobSemaphore;
    private final ProductRepo productRepo;
    private final UserAgentService userAgentService;
    private final InfaticaProps infaticaProps;
    private final ObjectMapper objectMapper;
    private final ScraperProps scraperProps;
    private final SmartProxyProps smartProxyProps;
    private final TaskRepo taskRepo;
    private final JobUtils jobUtils;
    private final RabbitMQProps rabbitMQProps;

    @SneakyThrows
    @Override
    public void scrape(String jobId, String sku, String url, JobType type, boolean scheduleJobs, int numRetries,
        boolean headless, boolean initialReviewByStarRating, String starRating) {
        log.info("Scraping {} at {}", sku, url);
        String soup = null;
        try {
            if (headless) {
                soup = downloadPageHeadless(url);
            } else {
                soup = request(url);
            }
        } catch (Exception e) {
            log.error("{}: {} - {}", e.getLocalizedMessage(), jobId, url);
        } finally {
            jobSemaphore.getScrapeJobSemaphore().release();
        }
        // Although we want to retry if parsing any of the pages fails, it is okay to skip failing
        // ones. The most important page is the product page. But the HTML soup for that is
        // provided by the client.
        parseSoup(soup,jobId, sku, url, type, scheduleJobs, numRetries,
            initialReviewByStarRating,
            starRating);
    }

    private void parseSoup(String soup, String jobId, String sku, String url,
        JobType type, boolean scheduleJobs, int numRetries, boolean initialReviewByStarRating,
        String starRating) {
        Merchant merchant = getPlatform(url);
        if (soup != null) {
            final String _soup = soup;
            Thread.startVirtualThread(() -> {
                if (Objects.requireNonNull(merchant) == Merchant.AMAZON) {
                    ProductEntity entity = productRepo.findByProductSku(sku);
                    if (scraperProps.isSaveHtml()) {
                        saveFile(_soup,  url);
                    }
                    switch (type) {
                        case PRODUCT_PAGE -> amazonParser.parseProductPage(sku, _soup, scheduleJobs,
                            null);
                        case QUESTION_PAGE -> amazonParser.parseProductQuestions(entity, _soup, scheduleJobs);
                        case REVIEW_PAGE -> amazonParser.parseReviewPage(entity, _soup, initialReviewByStarRating, starRating,
                            numRetries);
                        case ANSWER_PAGE -> amazonParser.parseProductAnswers(sku, _soup);
                    }
                }
            });
        }
    }

    @Override
    public void smartProxyScraper(ScrapingJob job) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUrl(job.getUrl());
        taskEntity.setJobType(job.getType().name());
        taskEntity.setProductId(job.getProductId());
        taskEntity.setStarRating(job.getStarRating());
        taskEntity.setQuestionId(job.getQuestionId());
        SmartProxyJob smartJob = new SmartProxyJob();
        try {
            log.info("Scheduling job={} url={}", job.getType().name(), job.getUrl());
            String taskId = submitSmartProxyTask(job.getUrl());
            taskEntity.setTaskId(taskId);
            taskRepo.save(taskEntity);
            smartJob.setUrl(job.getUrl());
            smartJob.setId(job.getId());
            smartJob.setProductId(job.getProductId());
            smartJob.setProductSku(job.getProductSku());
            smartJob.setQuestionId(job.getQuestionId());
            smartJob.setStarRating(job.getStarRating());
            smartJob.setInitialReviewsByStarRating(job.isInitialReviewsByStarRating());
            smartJob.setRetries(job.getRetries());
            smartJob.setType(job.getType());
            smartJob.setTaskId(taskId);
            smartJob.setResultUrl(smartProxyProps.getResultUrl()
                .replace("\"", "")
                .replace("{}", taskId));
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            jobSemaphore.getScrapeJobSemaphore().release();
        }
        scheduleSmartProxyJob(smartJob);
    }

    @Override
    public void smartProxyResultHandler(SmartProxyJob job) {
        boolean reschedule = false;
        try {
            getSmartProxyResult(job.getResultUrl());
            String result = getSmartProxyResult(job.getResultUrl());
            if (result == null) {
                reschedule = true;
            } else if (result.toLowerCase().startsWith("no content")) {
                TaskEntity taskEntity = taskRepo.findByTaskId(job.getTaskId());
                taskEntity.setCompleted(true);
                taskRepo.save(taskEntity);
            } else {
                SmartProxyResultsDto resultsDto = objectMapper.readValue(result,
                    SmartProxyResultsDto.class);
                if (ObjectUtils.isEmpty(resultsDto.getResults().get(0).getContent())) {
                    reschedule = true;
                } else {
                    log.info("Result for taskId={} job={} url={}", job.getTaskId(), job.getType().name(),
                        job.getUrl());
                    TaskEntity taskEntity = taskRepo.findByTaskId(job.getTaskId());
                    taskEntity.setCompleted(true);
                    taskRepo.save(taskEntity);
                    parseSoup(resultsDto.getResults().get(0).getContent(),
                        job.getId(), job.getProductSku(), job.getUrl(), job.getType(),
                        true,
                        job.getRetries(),
                        job.isInitialReviewsByStarRating(),
                        job.getStarRating());
                }
            }
        } catch (Exception e) {
            log.warn("Result not found for taskId={}. Rescheduling job", job.getTaskId());
            reschedule = true;
        } finally {
            jobSemaphore.getSmartProxyJobSemaphore().release();
        }
        if (reschedule) {
            scheduleSmartProxyJob(job);
        }
    }

    private void scheduleSmartProxyJob(SmartProxyJob job) {
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(jobUtils.getRandomThrottle());
            } catch (InterruptedException e) {
                // pass
            }
            jobUtils.submitJob(job, rabbitMQProps
                .getJobQueues()
                .get(RabbitMQProps.SMART_PROXY_JOB_QUEUE_KEY)
                .getRoutingKeyPrefix());
        });
    }

    private String downloadPageHeadless(String url) {
        WebClient client = scraperUtils.getWebClient();
        HtmlPage page;
        try {
            page = client.getPage(url);
            int statusCode = page.getWebResponse().getStatusCode();
            if (statusCode >= 400) {
                throw new RuntimeException(
                    String.format("Status Code: %s URL: %s", statusCode, url));
            }
            return page.getWebResponse().getContentAsString();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    private String request(String url) throws IOException {
        String soup = null;
        if (url != null) {
            List<Header> headers = getHeaders();
            try (CloseableHttpClient httpClient = HttpClients
                .custom()
                .setDefaultHeaders(headers)
                .setUserAgent(userAgentService.getRandomUserAgent())
//                .setProxy(HttpHost.create(scraperProps.getRandomProxy()))
                .build()) {
                HttpPost httpPost = new HttpPost(infaticaProps.getUrl());
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                StringEntity body = getBody(headers, url);
                httpPost.setEntity(body);
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                soup = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    throw new RuntimeException(
                        String.format("Status Code: %s URL: %s", statusCode, url));
                }
                httpResponse.close();
            }
        }
        return soup;
    }

    private String submitSmartProxyTask(String url) throws IOException {
        if (url != null) {
            try (CloseableHttpClient httpClient = HttpClients
                .custom()
                .setUserAgent(userAgentService.getRandomUserAgent())
                .setDefaultHeaders(getSmartProxyHeaders(true))
                .build()) {
                HttpPost httpPost = new HttpPost(smartProxyProps.getRequestUrl());
                StringEntity body = getSmartProxyBody(url);
                httpPost.setEntity(body);
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                String result = EntityUtils.toString(httpResponse.getEntity());
                SmartProxyTaskDto dto = objectMapper.readValue(result, SmartProxyTaskDto.class);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    throw new RuntimeException(
                        String.format("Status Code: %s URL: %s", statusCode, url));
                }
                httpResponse.close();
                return dto.getId();
            }
        }
        return null;
    }

    private String getSmartProxyResult(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients
            .custom()
            .setUserAgent(userAgentService.getRandomUserAgent())
            .setDefaultHeaders(getSmartProxyHeaders(false))
            .build()) {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            var entity = httpResponse.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    throw new RuntimeException(
                        String.format("Status Code: %s URL: %s", statusCode, url));
                }
            }
            httpResponse.close();
            return result;
        }
    }

    private StringEntity getSmartProxyBody(String url) {
        Map<String, Object> body = new HashMap<>();
        body.put("target", "amazon");
        body.put("parse", false);
        body.put("url", url);
//        body.put("callback_url", true);
        try {
            return new StringEntity(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    private StringEntity getBody(List<Header> headers, String url) {
        Map<String, Object> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("api_key", infaticaProps.getKey());
        body.put("url", url);
        body.put("render_js", true);
        body.put("return_json", false);
        body.put("headers", headerMap);
        try {
            return new StringEntity(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    private List<Header> getHeaders() {
        int min = 600;
        int max = 920;
        int width = new Random().nextInt((max - min) + 1) + min;
        return List.of(
            new BasicHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"),
            new BasicHeader(HttpHeaders.CONTENT_LANGUAGE, "en"),
            new BasicHeader(HttpHeaders.CONTENT_LANGUAGE, "en"),
            new BasicHeader(HttpHeaders.USER_AGENT, userAgentService.getRandomUserAgent()),
            new BasicHeader("sec-fetch-dest", "document"),
            new BasicHeader("sec-fetch-mode", "navigate"),
            new BasicHeader("sec-fetch-site", "same-origin"),
            new BasicHeader("sec-ch-viewport-width", String.valueOf(width)),
            new BasicHeader("viewport-width", String.valueOf(width)),
            new BasicHeader("upgrade-insecure-requests", String.valueOf(1))
        );
    }

    private List<Header> getSmartProxyHeaders(boolean initialRequest) {
        List<Header> headers = new ArrayList<>(List.of(
            new BasicHeader(HttpHeaders.ACCEPT, "application/json"),
            new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + smartProxyProps.getToken())));
        if (initialRequest) {
            headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        }
        return headers;
    }

    private void saveFile(String html, String path) {
        path = "product/" + path
            .replace("//:", "_")
            .replace("/", "_")
            .replace(":", "")
            .replace(".", "_") + ".html";
        try (InputStream in = new ByteArrayInputStream(html.getBytes(UTF8))) {
            // Save the file to disk
            if (!new File(path).exists()) {
                new File(path).mkdirs();
            }
            File output = new File(path);
            Files.copy(in, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ".saveFile: {}",
                e.getLocalizedMessage());
        }
    }


    private Merchant getPlatform(String url) {
        URL parsedUrl = null;
        try {
            parsedUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (parsedUrl != null) {
            if (parsedUrl.getHost().toLowerCase().contains("amazon")) {
                return Merchant.AMAZON;
            }
        }
        return Merchant.AMAZON;
    }
}
