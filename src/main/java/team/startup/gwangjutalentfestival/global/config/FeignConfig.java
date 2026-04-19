package team.startup.gwangjutalentfestival.global.config;

import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign 클라이언트 전역 설정.
 * <p>프로퍼티({@code feign.log-level})를 통해 Feign 로그 레벨을 동적으로 설정한다. 기본값은 {@code BASIC}이다.</p>
 */
@Configuration
public class FeignConfig {

    @Value("${feign.log-level:BASIC}")
    private String logLevel;

    /**
     * Feign 클라이언트 로그 레벨을 설정한다.
     *
     * @return {@link feign.Logger.Level} 빈
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.valueOf(logLevel);
    }
}
