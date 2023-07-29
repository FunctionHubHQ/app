package com.gptlambda.api.service.runtime;

import com.gptlambda.api.Code;
import com.gptlambda.api.CodeUpdateResponse;
import com.gptlambda.api.ExecRequest;
import com.gptlambda.api.ExecResult;
import com.gptlambda.api.GenericResponse;
import com.gptlambda.api.SpecResult;

/**
 * @author Biz Melesse created on 7/26/23
 */
public interface RuntimeService {
  GenericResponse exec(ExecRequest execRequest);
  String getUserCode(String uid);
  GenericResponse handleExecResult(ExecResult execResult);
  String generateCodeVersion();
  CodeUpdateResponse updateCode(Code code);
  Code getCodeDetail(String uid);
  String deleteMe(String uid);
  String generateOpenApiSpec(String tsInterface);
  GenericResponse handleSpecResult(SpecResult specResult);
}
