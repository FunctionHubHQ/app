package com.gptlambda.api.service.chromeExtension;

import com.gptlambda.api.ExtensionRequest;
import com.gptlambda.api.GenericResponse;

/**
 * @author Biz Melesse created on 6/15/23
 */
public interface ExtensionService {
  GenericResponse handleMessages(ExtensionRequest request);
}
