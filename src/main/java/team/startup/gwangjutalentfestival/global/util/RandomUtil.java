package team.startup.gwangjutalentfestival.global.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String createRandomCode(int length) {
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        }

        return sb.toString();
    }
}
