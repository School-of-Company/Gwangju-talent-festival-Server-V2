package team.startup.gwangjutalentfestival.global.sms.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SMS 인증 관련 설정 프로퍼티.
 * <p>최대 발송 횟수, 인증 코드/횟수 TTL, Redis 키 프리픽스를 {@code sms.verify.*}로 바인딩한다.</p>
 */
@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "sms.verify")
public class SmsVerifyProperties {
    private int maxSendCount;
    private long verifyCodeTtl;
    private long verifyCountTtl;
    private String verifyCountKeyPrefix;

    /**
     * 전화번호에 해당하는 인증 횟수 Redis 키를 반환한다.
     *
     * @param phoneNumber 전화번호
     * @return Redis 키 문자열
     */
    public String getVerifyCountKey(String phoneNumber) {
        return verifyCountKeyPrefix + phoneNumber;
    }
}
