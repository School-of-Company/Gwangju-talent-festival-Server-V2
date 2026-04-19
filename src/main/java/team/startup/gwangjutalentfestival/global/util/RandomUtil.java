package team.startup.gwangjutalentfestival.global.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 보안 난수 기반 코드 생성 유틸리티.
 * <p>{@link SecureRandom}을 사용하여 예측 불가능한 숫자 코드를 생성한다.</p>
 */
@Component
public class RandomUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 지정된 길이의 숫자 랜덤 코드를 생성한다.
     *
     * @param length 생성할 코드 길이
     * @return 숫자로만 이루어진 랜덤 코드 문자열
     */
    public String createRandomCode(int length) {
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        }

        return sb.toString();
    }
}
