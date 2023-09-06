package net.functionhub.api.service.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.data.postgres.entity.RequestHistoryEntity;
import net.functionhub.api.data.postgres.repo.RequestHistoryRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse created on 9/5/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalServiceImpl implements InternalService {
  private final RequestHistoryRepo requestHistoryRepo;

  @Override
//  @Async
  public GenericResponse logHttpRequests(Map<String, Object> requestLog) {
    RequestHistoryEntity requestHistory = new RequestHistoryEntity();
    requestHistory.setId(UUID.randomUUID());
    requestHistory.setUserId(objectToString(requestLog.get("userId")));
    requestHistory.setHttpMethod(objectToString(requestLog.get("httpMethod")));
    requestHistory.setUrl(objectToString(requestLog.get("url")));
    requestHistory.setErrorMessage(objectToString(requestLog.get("errorMessage")));
    requestHistory.setRequestStartedAt(objectToLocalDateTime(requestLog.get("requestStartedAt")));
    requestHistory.setRequestEndedAt(objectToLocalDateTime(requestLog.get("requestEndedAt")));
    requestHistory.setRequestDuration(objectToInt(requestLog.get("requestDuration")));
    requestHistory.setHttpStatusCode(objectToInt(requestLog.get("httpStatusCode")));
    requestHistory.setExecutionId(objectToString(requestLog.get("executionId").toString()));
    requestHistory.setRequestContentLength(objectToInt(requestLog.get("requestContentLength")));
    requestHistory.setResponseContentLength(objectToInt(requestLog.get("responseContentLength")));
    requestHistoryRepo.save(requestHistory);
    return new GenericResponse().status("ok");
  }

  private LocalDateTime objectToLocalDateTime(Object o) {
    Long timestamp = objectToLong(o);
    if (timestamp != null) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
    }
    return null;
  }

  private Long objectToLong(Object o) {
    if (!ObjectUtils.isEmpty(o)) {
      return (long) o;
    }
    return null;
  }

  private Integer objectToInt(Object o) {
    if (!ObjectUtils.isEmpty(o)) {
      return (int) o;
    }
    return null;
  }

  private String objectToString(Object o) {
    if (!ObjectUtils.isEmpty(o)) {
      return o.toString();
    }
    return null;
  }
}
