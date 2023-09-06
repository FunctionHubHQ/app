package net.functionhub.api.service.internal;

import java.util.Map;
import net.functionhub.api.GenericResponse;

/**
 * @author Biz Melesse created on 9/5/23
 */
public interface InternalService {
  GenericResponse logHttpRequests(Map<String, Object> requestLog);
}
