package team.startup.gwangjutalentfestival.domain.performer.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.performer.entity.PerformerVerificationEntity;
import team.startup.gwangjutalentfestival.domain.performer.exception.AlreadyPerformerException;
import team.startup.gwangjutalentfestival.domain.performer.exception.InvalidPerformerVerificationException;
import team.startup.gwangjutalentfestival.domain.performer.exception.PerformerVerificationAlreadyClaimedException;
import team.startup.gwangjutalentfestival.domain.performer.presentation.data.request.VerifyPerformerRequest;
import team.startup.gwangjutalentfestival.domain.performer.repository.PerformerVerificationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerifyPerformerServiceImplTest {

    private static final long USER_ID = 1L;
    private static final String NAME = "홍길동";
    private static final String CODE = "secure-verification-code";

    @Mock
    private PerformerVerificationRepository performerVerificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties = new JwtProperties("secret", 1000L, 1209600L);

    @BeforeEach
    void setUp() {
        CustomUserDetails details = CustomUserDetails.fromToken(USER_ID, Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 올바른_인증정보면_현재_계정에_PERFORMER_권한과_새_토큰을_부여한다() throws Exception {
        PerformerVerificationEntity verification = verification(NAME, LocalDateTime.now().plusDays(1), null);
        UserEntity user = user(Role.USER);
        TokenResponse response = new TokenResponse("access", LocalDateTime.now(), "refresh", LocalDateTime.now(), Role.PERFORMER);
        given(performerVerificationRepository.findByCodeHashForUpdate(hash(CODE))).willReturn(Optional.of(verification));
        given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user));
        given(jwtProvider.receiveToken(USER_ID, Role.PERFORMER)).willReturn(response);

        TokenResponse result = service().execute(new VerifyPerformerRequest(NAME, CODE));

        assertThat(result).isSameAs(response);
        assertThat(user.getRole()).isEqualTo(Role.PERFORMER);
        assertThat(verification.getClaimedUserId()).isEqualTo(USER_ID);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("refresh");
    }

    @Test
    void 이름이_다르면_인증을_거부한다() throws Exception {
        given(performerVerificationRepository.findByCodeHashForUpdate(hash(CODE)))
                .willReturn(Optional.of(verification("다른 이름", LocalDateTime.now().plusDays(1), null)));

        assertThatThrownBy(() -> service().execute(new VerifyPerformerRequest(NAME, CODE)))
                .isInstanceOf(InvalidPerformerVerificationException.class);
    }

    @Test
    void 만료된_코드면_인증을_거부한다() throws Exception {
        given(performerVerificationRepository.findByCodeHashForUpdate(hash(CODE)))
                .willReturn(Optional.of(verification(NAME, LocalDateTime.now().minusSeconds(1), null)));

        assertThatThrownBy(() -> service().execute(new VerifyPerformerRequest(NAME, CODE)))
                .isInstanceOf(InvalidPerformerVerificationException.class);
    }

    @Test
    void 이미_사용된_코드면_인증을_거부한다() throws Exception {
        given(performerVerificationRepository.findByCodeHashForUpdate(hash(CODE)))
                .willReturn(Optional.of(verification(NAME, LocalDateTime.now().plusDays(1), 2L)));

        assertThatThrownBy(() -> service().execute(new VerifyPerformerRequest(NAME, CODE)))
                .isInstanceOf(PerformerVerificationAlreadyClaimedException.class);
    }

    @Test
    void 이미_출연진인_계정이면_인증을_거부한다() throws Exception {
        given(performerVerificationRepository.findByCodeHashForUpdate(hash(CODE)))
                .willReturn(Optional.of(verification(NAME, LocalDateTime.now().plusDays(1), null)));
        given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user(Role.PERFORMER)));

        assertThatThrownBy(() -> service().execute(new VerifyPerformerRequest(NAME, CODE)))
                .isInstanceOf(AlreadyPerformerException.class);
    }

    private VerifyPerformerServiceImpl service() {
        return new VerifyPerformerServiceImpl(
                performerVerificationRepository, userRepository, jwtProvider,
                refreshTokenRepository, jwtProperties);
    }

    private PerformerVerificationEntity verification(String name, LocalDateTime expiresAt, Long claimedUserId) {
        return PerformerVerificationEntity.builder()
                .participantName(name)
                .codeHash("hash")
                .claimedUserId(claimedUserId)
                .expiresAt(expiresAt)
                .build();
    }

    private UserEntity user(Role role) {
        return UserEntity.builder()
                .id(USER_ID)
                .phoneNumber("01012345678")
                .password("encoded")
                .role(role)
                .build();
    }

    private String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
