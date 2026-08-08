package team.startup.gwangjutalentfestival.domain.performer.service.impl;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.performer.entity.PerformerVerificationEntity;
import team.startup.gwangjutalentfestival.domain.performer.exception.PerformerVerificationAlreadyClaimedException;
import team.startup.gwangjutalentfestival.domain.performer.presentation.data.request.VerifyPerformerRequest;
import team.startup.gwangjutalentfestival.domain.performer.repository.PerformerVerificationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class PerformerVerificationConcurrencyTest {

    private static final String NAME = "홍길동";
    private static final String CODE = "secure-verification-code";

    @Autowired
    private VerifyPerformerServiceImpl verifyPerformerService;
    @Autowired
    private PerformerVerificationRepository performerVerificationRepository;
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockBean
    private Sheets sheets;
    @MockBean
    private Drive drive;

    @AfterEach
    void tearDown() {
        performerVerificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 동일_코드_동시_인증시_한_계정만_성공한다() throws Exception {
        UserEntity first = userRepository.save(user("01011111111"));
        UserEntity second = userRepository.save(user("01022222222"));
        performerVerificationRepository.save(PerformerVerificationEntity.builder()
                .participantName(NAME)
                .codeHash(hash(CODE))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());
        given(jwtProvider.receiveToken(any(Long.class), eq(Role.PERFORMER))).willAnswer(invocation ->
                new TokenResponse("access", LocalDateTime.now(), "refresh", LocalDateTime.now(), Role.PERFORMER));

        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Throwable> firstResult = executor.submit(() -> verify(first.getId(), ready, start, successCount));
            Future<Throwable> secondResult = executor.submit(() -> verify(second.getId(), ready, start, successCount));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(Stream.of(firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS))
                    .filter(failure -> failure != null)
                    .toList())
                    .singleElement()
                    .isInstanceOf(PerformerVerificationAlreadyClaimedException.class);
        }

        PerformerVerificationEntity verification = performerVerificationRepository.findAll().getFirst();
        assertThat(successCount).hasValue(1);
        assertThat(verification.getClaimedUserId()).isIn(first.getId(), second.getId());
    }

    private Throwable verify(Long userId, CountDownLatch ready, CountDownLatch start, AtomicInteger successCount) {
        CustomUserDetails details = CustomUserDetails.fromToken(userId, Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        ready.countDown();
        try {
            start.await();
            verifyPerformerService.execute(new VerifyPerformerRequest(NAME, CODE));
            successCount.incrementAndGet();
            return null;
        } catch (Throwable failure) {
            return failure;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private UserEntity user(String phoneNumber) {
        return UserEntity.builder()
                .phoneNumber(phoneNumber)
                .password("encoded")
                .role(Role.USER)
                .build();
    }

    private String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
