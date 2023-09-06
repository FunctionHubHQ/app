package net.functionhub.proxy.service.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.proxy.dto.FHAccessToken;
import net.functionhub.proxy.dto.HttpRequestHistory;
import net.functionhub.proxy.dto.UserHeaders;
import net.functionhub.proxy.props.ApiProps;
import net.functionhub.proxy.service.proxy.ProxyService;
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
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

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
  private final ApiProps apiProps;
  private final RedisTemplate<String, Object> redisTemplate;
  private final String X_FUNCTION_HUB_ACCESS_TOKEN = "X-Function-Hub-Access-Token";
  private final String X_FUNCTION_HUB_PROXY_TARGET = "X-Function-Hub-Proxy-Target";

  @Override
  public void handler()  {
    HttpRequestHistory requestHistory = new HttpRequestHistory();
    try {
      requestHistory.setRequestContentLength((long) httpServletRequest.getContentLength());
      UserHeaders userHeaders = getHeaders();
      FHAccessToken accessToken = decodeAccessToken(userHeaders.getAccessToken());
      if (accessToken != null) {
        requestHistory.setUserId(accessToken.getUid());
        requestHistory.setHttpMethod(httpServletRequest.getMethod());
        requestHistory.setUrl(userHeaders.getProxyTarget());
        requestHistory.setExecutionId(accessToken.getRid());
      }
      if (ObjectUtils.isEmpty(userHeaders) ||
          ObjectUtils.isEmpty(userHeaders.getHeaders())) {
        String errorMsg = "Encountered an unknown error";
        requestHistory.setErrorMessage(errorMsg);
        requestHistory.setHttpStatusCode(500);
        logRequestHistory(requestHistory);
        setError(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMsg);
      } else {
        if (ObjectUtils.isEmpty(userHeaders.getProxyTarget())) {
          String errorMsg = "URL cannot be empty";
          requestHistory.setErrorMessage(errorMsg);
          requestHistory.setHttpStatusCode(500);
          logRequestHistory(requestHistory);
          setError(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMsg);
        } else if (ObjectUtils.isEmpty(userHeaders.getAccessToken())) {
          String errorMsg = "Missing " + X_FUNCTION_HUB_ACCESS_TOKEN;
          requestHistory.setErrorMessage(errorMsg);
          requestHistory.setHttpStatusCode(400);
          logRequestHistory(requestHistory);
          setError(HttpStatus.BAD_REQUEST_400, errorMsg);
        } else {
          try {
            if (accessToken == null) {
              String errorMsg = "Error processing access token";
              requestHistory.setErrorMessage(errorMsg);
              requestHistory.setHttpStatusCode(500);
              logRequestHistory(requestHistory);
              setError(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMsg);
              return;
            }
            if (!(contentLengthUnderLimit(accessToken, httpServletRequest.getContentLength(),
                requestHistory) &&
                httpCallCountUnderLimit(accessToken, userHeaders.getAccessToken(),
                    requestHistory))) {
              logRequestHistory(requestHistory);
              return;
            }
            boolean set500 = false;
            long start = Instant.now().toEpochMilli();
            HttpEntity responseEntity = null;
            try {
              responseEntity = forwardRequest(userHeaders);
            } catch (IOException e) {
              set500 = true;
            }
            long end = Instant.now().toEpochMilli();
            long elapsed = end - start;
            requestHistory.setRequestStartedAt(start);
            requestHistory.setRequestEndedAt(end);
            requestHistory.setRequestDuration(elapsed);

            if (set500) {
              requestHistory.setHttpStatusCode(500);
              httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
              requestHistory.setHttpStatusCode(httpServletResponse.getStatus());
            }

            // Write response body
            if (responseEntity != null) {
              requestHistory.setResponseContentLength(responseEntity.getContentLength());
              // Enforce data transfer limit on responses as well
              if (!contentLengthUnderLimit(accessToken, responseEntity.getContentLength(),
                  requestHistory)) {
                logRequestHistory(requestHistory);
                return;
              }
              try {
                responseEntity.writeTo(httpServletResponse.getOutputStream());
                logRequestHistory(requestHistory);
              } catch (IOException e) {
                requestHistory.setHttpStatusCode(500);
                requestHistory.setErrorMessage("Error writing response to output stream");
                logRequestHistory(requestHistory);
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              }
            }
          } catch (Exception e) {
            String errorMessage = "Error processing HTTP request";
            requestHistory.setErrorMessage(errorMessage);
            requestHistory.setHttpStatusCode(500);
            logRequestHistory(requestHistory);
            setError(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMessage);
          }
        }
      }
    } catch (Exception e) {
      String errorMsg = "Encountered internal error while processing request";
      requestHistory.setErrorMessage(errorMsg);
      requestHistory.setHttpStatusCode(500);
      logRequestHistory(requestHistory);
      setError(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMsg);
    }
  }

  private void logRequestHistory(HttpRequestHistory requestHistory) {
    Thread.startVirtualThread(() -> {
      try {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiProps.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<Object> request =
            new org.springframework.http.HttpEntity<>(new Gson().toJson(requestHistory), headers);
        restTemplate.postForObject(apiProps.getLogUrl(), request, String.class);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void setError(int errorCode, String msg) {
    httpServletResponse.setStatus(errorCode);
    httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
    Map<String, String> message = new HashMap<>();
    message.put("error", msg);
    try {
      objectMapper.writeValue(httpServletResponse.getWriter(), message);
    } catch (IOException e) {
      httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
    String accessToken = null;
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      if (header.equalsIgnoreCase(X_FUNCTION_HUB_PROXY_TARGET)) {
        fhProxyTarget = httpServletRequest.getHeader(header);
      } else if (header.equalsIgnoreCase(X_FUNCTION_HUB_ACCESS_TOKEN)) {
        accessToken = httpServletRequest.getHeader(header);
      } else if (!header.equalsIgnoreCase("host")) {
        rawHeaders.put(header, httpServletRequest.getHeader(header));
      }
    }
    userHeaders.setHeaders(rawHeaders);
    userHeaders.setProxyTarget(fhProxyTarget);
    userHeaders.setAccessToken(accessToken);
    return userHeaders;
  }

  @Override
  public boolean contentLengthUnderLimit(FHAccessToken accessToken, long contentLength,
      HttpRequestHistory requestHistory) {
    if (contentLength > accessToken.getDtl()) {
      String errorMsg = "Data transfer limit exceeded";
      requestHistory.setErrorMessage(errorMsg);
      requestHistory.setHttpStatusCode(403);
      setError(HttpStatus.FORBIDDEN_403, errorMsg);
      return false;
    }
    return true;
  }

  @Override
  public boolean httpCallCountUnderLimit(FHAccessToken accessToken, String accessTokenStr,
      HttpRequestHistory requestHistory) {
    long numHttpCalls = getHttpCallCount(accessTokenStr);
    if (numHttpCalls >= accessToken.getHce()) {
      String errorMsg = "You've reached the number of allowed HTTP calls for this invocation";
      requestHistory.setErrorMessage(errorMsg);
      requestHistory.setHttpStatusCode(403);
      setError(HttpStatus.FORBIDDEN_403, errorMsg);
      return false;
    }
    setHttpCallCount(accessTokenStr, numHttpCalls);
    return true;
  }


  @Override
  public FHAccessToken decodeAccessToken(String accessToken) {
    try {
      String decodedAccessToken = new String(Base64.getDecoder().decode(accessToken.getBytes()));
      return objectMapper.readValue(decodedAccessToken, FHAccessToken.class);
    } catch (JsonProcessingException | NullPointerException e) {
      return null;
    }
  }

  @Override
  public void setHttpCallCount(String accessTokenStr, long currNumHttpCalls) {
    BoundValueOperations<String, Object> boundValueOperations = redisTemplate.boundValueOps(accessTokenStr);
    // Increment throws an error. Look into that.
    boundValueOperations.set(currNumHttpCalls + 1);
  }

  @Override
  public long getHttpCallCount(String accessTokenStr) {
    BoundValueOperations<String, Object> boundValueOperations = redisTemplate.boundValueOps(accessTokenStr);
    Object value = boundValueOperations.get();
    if (value != null) {
      return (long) value;
    }
    return Long.MAX_VALUE;
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
