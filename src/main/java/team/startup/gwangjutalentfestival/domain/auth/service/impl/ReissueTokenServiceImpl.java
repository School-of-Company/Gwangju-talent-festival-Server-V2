package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidRefreshTokenException;
import team.startup.gwangjutalentfestival.domain.auth.exception.RefreshTokenNotFoundException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
public class ReissueTokenServiceImpl implements ReissueTokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
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
