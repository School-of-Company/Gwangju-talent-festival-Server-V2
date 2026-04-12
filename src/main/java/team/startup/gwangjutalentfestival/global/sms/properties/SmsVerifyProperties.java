package team.startup.gwangjutalentfestival.global.sms.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "sms.verify")
public class SmsVerifyProperties {
    private int maxSendCount;
    private long verifyCodeTtl;
    private long verifyCountTtl;
    private String verifyCountKeyPrefix;

    public String getVerifyCountKey(String phoneNumber) {
        return verifyCountKeyPrefix + phoneNumber;
    }
}
