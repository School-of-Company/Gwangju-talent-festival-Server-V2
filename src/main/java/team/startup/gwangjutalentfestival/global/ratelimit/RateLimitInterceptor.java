package team.startup.gwangjutalentfestival.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.time.Duration;

/**
 * {@link RateLimited}가 붙은 엔드포인트에 사용자별 Redis 쿨다운을 적용하는 인터셉터.
 * <p>쿨다운 키를 {@code SETNX}로 선점하지 못하면 {@link TooManyRequestsException}을 던져
 * DB 접근 이전에 요청을 차단한다. Redis 장애 시에는 요청을 허용한다(fail-open).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.cooldown-seconds:1}")
    private long cooldownSeconds;

    private static final String COOLDOWN_PREFIX = "rate-limit:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;

        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) return true;

        String key = COOLDOWN_PREFIX + rateLimited.key() + ":" + UserUtil.getCurrentUserId();

        Boolean acquired;
        try {
            acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, "1", Duration.ofSeconds(cooldownSeconds));
        } catch (DataAccessException e) {
            log.error("Redis 쿨다운 확인 실패 - 요청을 허용합니다. key: {}", key, e);
            return true;
        }

        if (Boolean.FALSE.equals(acquired)) {
            throw new TooManyRequestsException();
        }
        return true;
    }
}
