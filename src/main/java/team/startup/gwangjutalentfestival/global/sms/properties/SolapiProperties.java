package team.startup.gwangjutalentfestival.global.sms.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "solapi")
public class SolapiProperties {
    private String apiKey;
    private String apiSecret;
    private String smsPhoneNumber;
    private String url;
}
