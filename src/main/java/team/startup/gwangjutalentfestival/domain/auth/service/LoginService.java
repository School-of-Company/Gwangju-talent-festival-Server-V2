package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.LoginRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;

/**
 * 로그인 처리 서비스 인터페이스.
 */
public interface LoginService {
    /**
     * 휴대폰 번호와 비밀번호로 로그인하고 JWT 토큰을 발급합니다.
     *
     * @param loginRequest 로그인 요청 정보
     * @return 발급된 AccessToken 및 RefreshToken 정보
     */
    TokenResponse execute(LoginRequest loginRequest);
}
