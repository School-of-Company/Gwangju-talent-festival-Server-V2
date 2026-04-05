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

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

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
