package team.startup.gwangjutalentfestival.domain.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

/**
 * Redis에 저장되는 RefreshToken 엔티티.
 * <p>사용자 ID를 키로 하며, TTL이 만료되면 자동으로 삭제됩니다.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@RedisHash(value = "refresh_token")
public class RefreshToken {

    /** Redis 키로 사용되는 사용자 ID */
    @Id
    private String userId;

    /** JWT RefreshToken 문자열 */
    @Indexed
    private String token;

    /** 만료 시간 (초 단위) */
    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiresIn;
}
