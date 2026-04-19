package team.startup.gwangjutalentfestival.domain.auth.service;

/**
 * 로그아웃 처리 서비스 인터페이스.
 */
public interface LogoutService {
    /**
     * 사용자의 RefreshToken을 삭제하고 AccessToken을 블랙리스트에 등록합니다.
     *
     * @param token 로그아웃할 사용자의 AccessToken 문자열
     */
    void execute(String token);
}
