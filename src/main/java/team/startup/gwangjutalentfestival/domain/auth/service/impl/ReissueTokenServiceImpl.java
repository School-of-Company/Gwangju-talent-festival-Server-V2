package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidRefreshTokenException;
import team.startup.gwangjutalentfestival.domain.auth.exception.RefreshTokenNotFoundException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

/**
 * {@link ReissueTokenService} 구현체.
 * <p>RefreshToken의 유효성을 검증하고 새로운 AccessToken과 RefreshToken을 재발급합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class ReissueTokenServiceImpl implements ReissueTokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * RefreshToken을 검증하고 기존 토큰을 교체한 뒤 새로운 JWT 토큰을 반환합니다.
     *
     * @param refreshToken 재발급에 사용할 RefreshToken 문자열
     * @return 새로 발급된 AccessToken 및 RefreshToken 정보
     * @throws InvalidRefreshTokenException RefreshToken이 유효하지 않거나 저장된 값과 다른 경우
     * @throws RefreshTokenNotFoundException Redis에서 RefreshToken을 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public TokenResponse execute(String refreshToken) {
        Claims claims;
        try {
            claims = jwtProvider.getClaims(refreshToken);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }

        if (!jwtProvider.isRefreshToken(claims)) {
            throw new InvalidRefreshTokenException();
        }
        Long userId = jwtProvider.getUserId(claims);
        Role role = Role.valueOf(jwtProvider.getRole(claims));

        RefreshToken stored = refreshTokenRepository.findById(String.valueOf(userId))
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!stored.getToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.deleteById(String.valueOf(userId));

        TokenResponse tokenResponse = jwtProvider.receiveToken(userId, role);

        RefreshToken newToken = RefreshToken.builder()
                .userId(String.valueOf(userId))
                .token(tokenResponse.refreshToken())
                .expiresIn(jwtProperties.getRefreshTokenExpiration())
                .build();

        refreshTokenRepository.save(newToken);

        return tokenResponse;
    }
}
