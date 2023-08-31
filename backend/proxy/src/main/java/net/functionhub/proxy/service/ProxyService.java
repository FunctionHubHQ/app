package net.functionhub.proxy.service;

import java.io.IOException;
import net.functionhub.proxy.dto.UserHeaders;
import org.apache.http.HttpEntity;

/**
 * @author Biz Melesse created on 8/30/23
 */
public interface ProxyService {
  void handler() throws IOException;
  HttpEntity forwardRequest(UserHeaders headers) throws IOException;
  UserHeaders getHeaders() throws IOException;
}
