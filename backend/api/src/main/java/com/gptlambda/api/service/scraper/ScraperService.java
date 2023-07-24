package com.gptlambda.api.service.scraper;

import com.gptlambda.api.dto.JobType;
import com.gptlambda.api.dto.ScrapingJob;
import com.gptlambda.api.dto.SmartProxyJob;

/**
 * @author Bizuwork Melesse
 * created on 6/17/22
 */
public interface ScraperService {

    /**
     * Get the media content at the given URL. Use WebClient to simulate a full browser and execute
     * any JavaScript code. Use JSoup for all other downstream tasks such as the parsing of the HTML
     * content and the downloading of the media content from the source.
     *
     * @param sku
     * @param url
     * @param numRetries
     * @return
     */
    void scrape(String jobId, String sku, String url, JobType type, boolean scheduleJobs, int numRetries, boolean headless,
        boolean initialReviewByStarRating, String starRating);

    void smartProxyScraper(ScrapingJob job);

    void smartProxyResultHandler(SmartProxyJob job);
}
