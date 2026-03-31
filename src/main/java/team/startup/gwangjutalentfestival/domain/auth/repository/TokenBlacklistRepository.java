package team.startup.gwangjutalentfestival.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistRepository {
    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void save(String jti, long remainingExpiration) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "logout",
                remainingExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.error("Redis 블랙리스트 조회 실패", e);
            return false;
        }
    }
}
