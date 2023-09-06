package net.functionhub.proxy.service.proxy;

import java.io.IOException;
import net.functionhub.proxy.dto.FHAccessToken;
import net.functionhub.proxy.dto.HttpRequestHistory;
import net.functionhub.proxy.dto.UserHeaders;
import org.apache.http.HttpEntity;

/**
 * @author Biz Melesse created on 8/30/23
 */
public interface ProxyService {
  void handler() throws IOException;
  HttpEntity forwardRequest(UserHeaders headers) throws IOException;
  UserHeaders getHeaders() throws IOException;
  boolean contentLengthUnderLimit(FHAccessToken accessToken, long contentLength, HttpRequestHistory requestHistory);
  boolean httpCallCountUnderLimit(FHAccessToken accessToken, String accessTokenStr, HttpRequestHistory requestHistory);
  FHAccessToken decodeAccessToken(String accessToken);
  void setHttpCallCount(String accessTokenStr, long currNumHttpCalls);
  long getHttpCallCount(String accessTokenStr);
}
