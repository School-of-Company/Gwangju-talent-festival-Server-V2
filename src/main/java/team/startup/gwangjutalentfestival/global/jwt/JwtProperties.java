package team.startup.gwangjutalentfestival.global.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * JWT 설정 프로퍼티.
 * <p>시크릿 키, 액세스 토큰 만료 시간, 리프레시 토큰 만료 시간을 {@code jwt.*} 프로퍼티로 바인딩한다.</p>
 */
@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
}
