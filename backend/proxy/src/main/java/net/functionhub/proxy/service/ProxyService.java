package net.functionhub.proxy.service;

import java.io.IOException;
import net.functionhub.proxy.dto.UserHttpRequest;

/**
 * @author Biz Melesse created on 8/30/23
 */
public interface ProxyService {
  void handler() throws IOException;
  UserHttpRequest getHeaders() throws IOException;
}
