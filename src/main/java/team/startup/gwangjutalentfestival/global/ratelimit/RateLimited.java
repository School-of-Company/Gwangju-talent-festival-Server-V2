package team.startup.gwangjutalentfestival.global.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자별 Redis 쿨다운 기반 요청 제한을 적용하는 어노테이션.
 * <p>쿨다운 시간 내 동일 사용자의 재요청은 429 Too Many Requests로 거절된다.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * 쿨다운 Redis 키를 구분하는 식별자 (예: "seat:reservation").
     *
     * @return 요청 제한 대상 식별자
     */
    String key();
}
