package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;

/**
 * JWT 토큰 재발급 서비스 인터페이스.
 */
public interface ReissueTokenService {
    /**
     * RefreshToken을 검증하고 새로운 AccessToken과 RefreshToken을 재발급합니다.
     *
     * @param refreshToken 재발급에 사용할 RefreshToken 문자열
     * @return 새로 발급된 AccessToken 및 RefreshToken 정보
     */
    TokenResponse execute(String refreshToken);
}
