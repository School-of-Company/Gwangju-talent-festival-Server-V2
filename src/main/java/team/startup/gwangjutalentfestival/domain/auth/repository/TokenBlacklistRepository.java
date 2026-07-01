package team.startup.gwangjutalentfestival.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 로그아웃된 AccessToken의 JTI(JWT ID)를 Redis 블랙리스트로 관리하는 레포지토리.
 * <p>토큰 만료 시간까지 블랙리스트에 보관하여 재사용을 방지합니다.</p>
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistRepository {
    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * 로그아웃된 AccessToken의 JTI를 블랙리스트에 등록합니다.
     *
     * @param jti                 블랙리스트에 등록할 JWT ID
     * @param remainingExpiration 토큰의 남은 만료 시간 (밀리초)
     */
    public void save(String jti, long remainingExpiration) {
        try {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jti,
                    "logout",
                    remainingExpiration,
                    TimeUnit.MILLISECONDS
            );
        } catch (DataAccessException e) {
            log.error("Redis 블랙리스트 등록 실패 (jti: {})", jti, e);
            throw e;
        }
    }

    /**
     * 해당 JTI가 블랙리스트에 등록되어 있는지 확인합니다.
     *
     * @param jti 확인할 JWT ID
     * @return 블랙리스트에 존재하면 {@code true}, 아니면 {@code false}
     */
    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
        } catch (Exception e) {
            log.error("Redis 블랙리스트 조회 실패", e);
            return false;
        }
    }
}
