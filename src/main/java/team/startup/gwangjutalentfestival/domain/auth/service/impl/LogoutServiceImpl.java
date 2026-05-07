package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidAccessTokenException;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.auth.repository.TokenBlacklistRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.LogoutService;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

/**
 * {@link LogoutService} 구현체.
 * <p>현재 사용자의 RefreshToken을 삭제하고, AccessToken을 블랙리스트에 등록하여 재사용을 방지합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    /**
     * 현재 인증된 사용자의 RefreshToken을 삭제하고 AccessToken을 블랙리스트에 등록합니다.
     *
     * @param token 로그아웃할 사용자의 AccessToken 문자열
     * @throws InvalidAccessTokenException 토큰 파싱에 실패한 경우
     */
    @Override
    @Transactional
    public void execute(String token) {
        Long userId = UserUtil.getCurrentUserId();
        refreshTokenRepository.deleteById(userId.toString());

        try {
            Claims claims = jwtProvider.getClaimsAllowExpired(token);
            long remaining = jwtProvider.getRemainingExpireTime(claims);
            String jti = claims.getId();
            if (jti != null && remaining > 0) {
                tokenBlacklistRepository.save(jti, remaining);
            }
        } catch (JwtException e) {
            throw new InvalidAccessTokenException();
        }
    }
}
