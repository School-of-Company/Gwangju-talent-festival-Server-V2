package team.startup.gwangjutalentfestival.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.auth.exception.InvalidRefreshTokenException;
import team.startup.gwangjutalentfestival.auth.exception.RefreshTokenNotFoundException;
import team.startup.gwangjutalentfestival.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
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

        Long userId = Long.parseLong(jwtProvider.getClaims(refreshToken).getSubject());
        String userIdStr = String.valueOf(userId);
        Role role = jwtProvider.getRole(refreshToken);

        RefreshToken stored = refreshTokenRepository.findById(userIdStr)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!stored.getToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.deleteById(userIdStr);

        TokenResponse tokenResponse = jwtProvider.receiveToken(userId, role);

        long expiresInSeconds = Duration.between(
                LocalDateTime.now(),
                tokenResponse.refreshTokenExpiresIn()
        ).getSeconds();

        RefreshToken newToken = RefreshToken.builder()
                .userId(userIdStr)
                .token(tokenResponse.refreshToken())
                .expiresIn(expiresInSeconds)
                .build();

        refreshTokenRepository.save(newToken);

        return tokenResponse;
    }
}
