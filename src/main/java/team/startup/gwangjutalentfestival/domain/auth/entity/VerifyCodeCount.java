package team.startup.gwangjutalentfestival.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("verify:count")
public class VerifyCodeCount {

    @Id
    private String phoneNumber;

    private Integer count;

    @TimeToLive
    private Integer ttl;

    public void increment() {
        this.count++;
    }

    public boolean isExceeded(int limit) {
        return this.count >= limit;
    }
}
