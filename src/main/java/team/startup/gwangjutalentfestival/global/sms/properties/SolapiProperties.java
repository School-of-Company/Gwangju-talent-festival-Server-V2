package team.startup.gwangjutalentfestival.global.sms.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Solapi SMS API 연동 설정 프로퍼티.
 * <p>API 키, 시크릿, 발신 번호, API URL을 {@code solapi.*}로 바인딩한다.</p>
 */
@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "solapi")
public class SolapiProperties {
    private String apiKey;
    private String apiSecret;
    private String smsPhoneNumber;
    private String url;
}
