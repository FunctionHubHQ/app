package com.gptlambda.api.service.scraper;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gptlambda.api.props.ScraperProps;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.springframework.stereotype.Service;

/**
 * @author Bizuwork Melesse
 * created on 6/22/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperUtils {
    private final ErrorListener errorListener;
    private final ScraperProps scraperProps;
    private final UserAgentService userAgentService;

    public WebClient getWebClient() {
        // User user agent
//        BrowserVersionBuilder browserVersion = new BrowserVersion.BrowserVersionBuilder(BrowserVersion.CHROME)
//            .setUserAgent(scraperProps.getUserAgent());
//        browserVersion.setUserAgent(scraperProps.getUserAgent());
        WebClient webClient = new WebClient(
//            browserVersion.build()
        );
        var headers = getHeaders();
        for (Header header : headers) {
            webClient.addRequestHeader(header.getName(), header.getValue());
        }

        // Set proxy
//        Pair<String, Integer> proxyIpWithPort = scraperProps.getProxyIpWithPort();
//        ProxyConfig proxyConfig = new ProxyConfig(proxyIpWithPort.getFirst(), proxyIpWithPort.getSecond(), "http");
//        webClient.getOptions().setProxyConfig(proxyConfig);

        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setTimeout(60000);
        webClient.setJavaScriptErrorListener(errorListener.javaScriptErrorListener());
        return webClient;
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
//            new BasicHeader("sec-ch-ua", "\"Chromium\";v=\"112\", \"Google Chrome\";v=\"112\", \"Not:A-Brand\";v=\"99\""),
            new BasicHeader("sec-fetch-dest", "document"),
            new BasicHeader("sec-fetch-mode", "navigate"),
            new BasicHeader("sec-fetch-site", "same-origin"),
            new BasicHeader("sec-ch-viewport-width", String.valueOf(width)),
            new BasicHeader("viewport-width", String.valueOf(width)),
            new BasicHeader("upgrade-insecure-requests", String.valueOf(1))
        );
    }
}
