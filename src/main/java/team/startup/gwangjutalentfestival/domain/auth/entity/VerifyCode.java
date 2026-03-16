package team.startup.gwangjutalentfestival.domain.auth.entity;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("verify:code")
public class VerifyCode {

    @Id
    private String phoneNumber;

    private String code;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private long ttl;
}
