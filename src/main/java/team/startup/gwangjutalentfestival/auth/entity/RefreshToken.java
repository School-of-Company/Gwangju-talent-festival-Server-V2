package team.startup.gwangjutalentfestival.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@RedisHash(value = "refresh_token")
public class RefreshToken {

    @Id
    private String userId;

    @Indexed
    private String token;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiresIn;
}
