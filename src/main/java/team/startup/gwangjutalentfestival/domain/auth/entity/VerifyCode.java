package team.startup.gwangjutalentfestival.domain.auth.entity;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

/**
 * Redis에 저장되는 SMS 인증번호 엔티티.
 * <p>휴대폰 번호를 키로 하며, TTL이 만료되면 자동으로 삭제됩니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("verify:code")
public class VerifyCode {

    /** Redis 키로 사용되는 휴대폰 번호 */
    @Id
    private String phoneNumber;

    /** 발급된 인증번호 문자열 */
    private String code;

    /** 만료 시간 (초 단위) */
    @TimeToLive(unit = TimeUnit.SECONDS)
    private long ttl;
}
