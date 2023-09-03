package net.functionhub.api.service.entitlement;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.service.utils.FHUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 9/3/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntitlementServiceImpl implements EntitlementService {
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void recordFunctionInvocation() {
    long timestamp = System.currentTimeMillis();

    String key = getUserKey();
    ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
    zSetOperations.add(key, timestamp, timestamp);
//     Invocation records are only valid for 1 minute
    String activityKey = key + ":" + timestamp;
    redisTemplate.expire(activityKey, Duration.ofSeconds(60));
  }

  @Override
  public long getNumFunctionInvocations(int lastNMinutes) {
    String key = getUserKey();
    long currentTime = System.currentTimeMillis();
    long timeThreshold = currentTime - TimeUnit.MINUTES.toMillis(lastNMinutes);
    ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
    Long value = zSetOperations.count(key, timeThreshold, currentTime);
    return value == null ? 0 : value;
  }

  private String getUserKey() {
    return INVOCATION_KEY_PREFIX + FHUtils.getSessionUser().getUid();
  }
}
