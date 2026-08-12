package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReissueTokenServiceImplTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Claims claims;

    @Test
    void 토큰의_기존_역할이_아닌_DB의_현재_역할로_재발급한다() {
        long userId = 1L;
        String oldRefreshToken = "old-refresh";
        JwtProperties properties = new JwtProperties("secret", 1000L, 1209600L);
        UserEntity user = UserEntity.builder().id(userId).role(Role.PERFORMER).build();
        RefreshToken stored = RefreshToken.builder().userId("1").token(oldRefreshToken).expiresIn(1209600L).build();
        TokenResponse issued = new TokenResponse(
                "new-access", LocalDateTime.now(), "new-refresh", LocalDateTime.now(), Role.PERFORMER);
        given(jwtProvider.getClaims(oldRefreshToken)).willReturn(claims);
        given(jwtProvider.isRefreshToken(claims)).willReturn(true);
        given(jwtProvider.getUserId(claims)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(refreshTokenRepository.findById("1")).willReturn(Optional.of(stored));
        given(jwtProvider.receiveToken(userId, Role.PERFORMER)).willReturn(issued);

        TokenResponse result = new ReissueTokenServiceImpl(
                jwtProvider, refreshTokenRepository, properties, userRepository).execute(oldRefreshToken);

        assertThat(result.role()).isEqualTo(Role.PERFORMER);
        verify(jwtProvider).receiveToken(userId, Role.PERFORMER);
    }
}
