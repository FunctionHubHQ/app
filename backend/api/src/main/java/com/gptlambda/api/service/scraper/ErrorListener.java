package com.gptlambda.api.service.scraper;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Bizuwork Melesse
 * created on 6/18/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorListener {

    public JavaScriptErrorListener javaScriptErrorListener()  {
        return new JavaScriptErrorListener() {
            @Override
            public void scriptException(HtmlPage page, ScriptException scriptException) {
                log.warn(scriptException.getLocalizedMessage());
            }

            @Override
            public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
                log.warn("Javascript execution timeout");
            }

            @Override
            public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
                log.warn(malformedURLException.getLocalizedMessage());
            }

            @Override
            public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
                log.warn(exception.getLocalizedMessage());
            }

            @Override
            public void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {
                log.warn(message);
            }
        };
    }
}
