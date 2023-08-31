package net.functionhub.proxy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.proxy.dto.UserHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;

/**
 * @author Biz Melesse created on 8/30/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {
  private final HttpServletRequest httpServletRequest;
  private final HttpServletResponse httpServletResponse;
  private final ObjectMapper objectMapper;

  @Override
  public void handler()  {
    UserHeaders userHeaders = getHeaders();
    if (ObjectUtils.isEmpty(userHeaders) ||
        ObjectUtils.isEmpty(userHeaders.getHeaders())) {
      httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
      Map<String, String> message = new HashMap<>();
      message.put("error", "Encountered an unknown error");
      try {
        objectMapper.writeValue(httpServletResponse.getWriter(), message);
      } catch (IOException e) {
        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    } else {
      if (ObjectUtils.isEmpty(userHeaders.getProxyTarget())) {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("error", "URL cannot be empty"); // This should really be handled by the http client
        try {
          objectMapper.writeValue(httpServletResponse.getWriter(), message);
        } catch (IOException e) {
          httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      } else if (ObjectUtils.isEmpty(userHeaders.getFhApiKey())) {
        httpServletResponse.setStatus(HttpStatus.BAD_REQUEST_400);
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> message = new HashMap<>();
        message.put("error", "Missing X-Function-Hub-Key header");
        try {
          objectMapper.writeValue(httpServletResponse.getWriter(), message);
        } catch (IOException e) {
          httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      } else {
        // 1. Do authorization and return 403 on error
        // 2. Check for entitlements, invocations, etc

        boolean set500 = false;
        StopWatch stopWatch = new StopWatch();
        HttpEntity responseEntity = null;
        int requestContentLength = httpServletRequest.getContentLength();
        try {
          stopWatch.start();
          responseEntity = forwardRequest(userHeaders);
        } catch (IOException e) {
          set500 = true;
        }
        stopWatch.stop();
        long elapsed = stopWatch.getLastTaskTimeMillis();

        if (set500) {
          httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Write response body
        if (responseEntity != null) {
          long responseContentLength = responseEntity.getContentLength();
          // TODO: make full request log here async
          log.info("Request took {} ms", elapsed);

          try {
            responseEntity.writeTo(httpServletResponse.getOutputStream());
          } catch (IOException e) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          }
        }
      }
    }
  }

  @Override
  public HttpEntity forwardRequest(UserHeaders headers) throws IOException {
    HttpClient httpClient = createHttpClientWithProxy();

    // Create a new HttpRequest by copying the incoming request's method and URI
    HttpUriRequest httpUriRequest = copyRequest(headers.getProxyTarget());

    // Copy headers from the incoming request to the forwarded request
    for (Entry<String, String> stringStringEntry : headers.getHeaders().entrySet()) {
      httpUriRequest.setHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
    }

    // Execute the forwarded request
    HttpResponse backendResponse = httpClient.execute(httpUriRequest);

    // Set the status code in the response
    int statusCode = ((HttpResponse) backendResponse).getStatusLine().getStatusCode();
    httpServletResponse.setStatus(statusCode);

    // Copy response headers from the backend response
    for (org.apache.http.Header header : backendResponse.getAllHeaders()) {
      httpServletResponse.setHeader(header.getName(), header.getValue());
    }
   return backendResponse.getEntity();
  }

  @Override
  public UserHeaders getHeaders() {
    UserHeaders userHeaders = new UserHeaders();
    // Extract proxy target and remove it from the rest of the user headers
    Enumeration<String> headers = httpServletRequest.getHeaderNames();
    Map<String, String> rawHeaders = new HashMap<>();
    String fhProxyTarget = null;
    String fhApiKey = null;
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      if (header.equalsIgnoreCase("X-Function-Hub-Proxy-Target")) {
        fhProxyTarget = httpServletRequest.getHeader(header);
      } else if (header.equalsIgnoreCase("X-Function-Hub-Key")) {
        fhApiKey = httpServletRequest.getHeader(header);
      } else if (!header.equalsIgnoreCase("host")) {
        rawHeaders.put(header, httpServletRequest.getHeader(header));
      }
    }
    userHeaders.setHeaders(rawHeaders);
    userHeaders.setProxyTarget(fhProxyTarget);
    userHeaders.setFhApiKey(fhApiKey);
    return userHeaders;
  }

  private HttpUriRequest copyRequest(String uri) {
    String method = httpServletRequest.getMethod().toUpperCase();
    try {
      switch (method) {
        case "GET":
          return new HttpGet(uri);
        case "POST":
          HttpPost postRequest = new HttpPost(uri);
          InputStreamEntity inputStreamEntity = new InputStreamEntity(
              httpServletRequest.getInputStream(), ContentType.APPLICATION_OCTET_STREAM);
          postRequest.setEntity(inputStreamEntity);
          return postRequest;
        case "PUT":
          HttpPut putRequest = new HttpPut(uri);
          inputStreamEntity = new InputStreamEntity(httpServletRequest.getInputStream(),
              ContentType.APPLICATION_OCTET_STREAM);
          putRequest.setEntity(inputStreamEntity);
          return putRequest;
        case "DELETE":
          return new HttpDelete(uri);
        case "OPTIONS":
          return new HttpOptions(uri);
        default:
          throw new UnsupportedOperationException("HTTP method not supported: " + method);
      }
    } catch (IOException e) {
      httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    return null;
  }

  private HttpClient createHttpClientWithProxy() {
    // Configure the proxy host and port
    String proxyHost = "proxy.example.com"; // Replace with the actual proxy host
    int proxyPort = 8080; // Replace with the actual proxy port

    // Create a RequestConfig with proxy settings
    RequestConfig requestConfig = RequestConfig.custom()
        .setProxy(new HttpHost(proxyHost, proxyPort))
        .build();

    // Create an HttpClient with the configured RequestConfig
//    return HttpClients.custom()
//        .setDefaultRequestConfig(requestConfig)
//        .build();
    return HttpClients.createDefault();
  }
}
