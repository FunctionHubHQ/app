package com.gptlambda.api.service.runtime;

import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.GenericResponse;

/**
 * @author Biz Melesse created on 7/26/23
 */
public interface RuntimeService {
  GenericResponse exec(ExecRequest execRequest);
  String getUserCode(String hash);
}
