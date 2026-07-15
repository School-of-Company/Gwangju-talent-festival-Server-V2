package team.startup.gwangjutalentfestival.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "test-secret-key-for-jwt-provider-unit-test-1234567890",
                3600L,
                3600L
        );
        jwtProvider = new JwtProvider(jwtProperties);
        jwtProvider.init();
    }

    @Test
    void 서명이_변조된_토큰은_예외_없이_유효하지_않은_토큰으로_판별한다() {
        String token = jwtProvider.generateAccessToken(1L, Role.USER);
        String tamperedToken = tamperSignature(token);

        assertThatCode(() -> jwtProvider.validateToken(tamperedToken)).doesNotThrowAnyException();
        assertThat(jwtProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void 정상_토큰은_유효한_토큰으로_판별한다() {
        String token = jwtProvider.generateAccessToken(1L, Role.USER);

        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    private String tamperSignature(String token) {
        String[] parts = token.split("\\.");
        return parts[0] + "." + parts[1] + "." + "invalidSignature";
    }
}
