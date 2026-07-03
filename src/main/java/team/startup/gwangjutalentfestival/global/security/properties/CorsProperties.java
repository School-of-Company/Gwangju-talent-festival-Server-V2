package team.startup.gwangjutalentfestival.global.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 허용 출처 설정 프로퍼티.
 * <p>{@code cors.allowed-origins} 프로퍼티로 허용할 출처 목록을 바인딩한다.</p>
 */
@Getter
@ConfigurationProperties(prefix = "cors")
@Setter
public class CorsProperties {
    private List<String> allowedOrigins;
}