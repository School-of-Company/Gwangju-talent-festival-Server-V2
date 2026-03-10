package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidRefreshTokenException;
import team.startup.gwangjutalentfestival.domain.auth.exception.RefreshTokenNotFoundException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReissueTokenServiceImpl implements ReissueTokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public TokenResponse execute(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        if (!jwtProvider.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        Long userId = jwtProvider.getUserId(claims);
        Role role = Role.valueOf(jwtProvider.getRole(claims));

        RefreshToken stored = refreshTokenRepository.findById(String.valueOf(userId))
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!stored.getToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.deleteById(String.valueOf(userId));

        TokenResponse tokenResponse = jwtProvider.receiveToken(userId, role);

        long expiresInSeconds = Duration.between(
                LocalDateTime.now(),
                tokenResponse.refreshTokenExpiresAt()
        ).getSeconds();

        RefreshToken newToken = RefreshToken.builder()
                .userId(String.valueOf(userId))
                .token(tokenResponse.refreshToken())
                .expiresIn(expiresInSeconds)
                .build();

        refreshTokenRepository.save(newToken);

        return tokenResponse;
    }
}
