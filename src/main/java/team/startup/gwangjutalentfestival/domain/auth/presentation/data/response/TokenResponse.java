package team.startup.gwangjutalentfestival.domain.auth.presentation.data.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.time.LocalDateTime;

/**
 * 로그인 및 토큰 재발급 시 반환되는 JWT 토큰 응답 DTO.
 *
 * @param accessToken            발급된 AccessToken 문자열
 * @param accessTokenExpiresAt   AccessToken 만료 일시
 * @param refreshToken           발급된 RefreshToken 문자열
 * @param refreshTokenExpiresAt  RefreshToken 만료 일시
 * @param role                   사용자 권한 역할
 */
public record TokenResponse(
        String accessToken,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime accessTokenExpiresAt,
        String refreshToken,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime refreshTokenExpiresAt,
        Role role
) {
}
