package team.startup.gwangjutalentfestival.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화를 위한 {@link PasswordEncoder} 빈 설정.
 * <p>BCrypt strength 12를 사용한다.</p>
 */
@Configuration
public class EncoderConfig {

    /**
     * BCrypt 기반 {@link PasswordEncoder} 빈을 생성한다.
     *
     * @return {@link PasswordEncoder} 빈
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
