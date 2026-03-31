package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.auth.repository.TokenBlacklistRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.LogoutService;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidAccessTokenException;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final HttpServletRequest request;

    @Override
    @Transactional
    public void execute() {
        Long userId = UserUtil.getCurrentUserId();
        refreshTokenRepository.deleteById(userId.toString());

        String token = jwtProvider.resolveToken(request);
        if (token != null) {
            try {
                Claims claims = jwtProvider.getClaimsAllowExpired(token);
                long remaining = jwtProvider.getRemainingExpireTime(claims);
                if (remaining > 0) {
                    tokenBlacklistRepository.save(claims.getId(), remaining);
                }
            } catch (JwtException e) {
                throw new InvalidAccessTokenException();
            }
        }
    }
}
