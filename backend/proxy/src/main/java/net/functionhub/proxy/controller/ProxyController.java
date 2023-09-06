package net.functionhub.proxy.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.proxy.service.proxy.ProxyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse created on 8/30/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProxyController {
  private final ProxyService proxyService;

  @RequestMapping(
      value = "/proxy",
      method = {
          RequestMethod.GET,
          RequestMethod.POST,
          RequestMethod.PUT,
          RequestMethod.DELETE,
          RequestMethod.OPTIONS,
      })
  public void proxyHandler() throws IOException {
    proxyService.handler();
  }
}
