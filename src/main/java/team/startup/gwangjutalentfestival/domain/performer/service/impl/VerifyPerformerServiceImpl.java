package team.startup.gwangjutalentfestival.domain.performer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.performer.entity.PerformerVerificationEntity;
import team.startup.gwangjutalentfestival.domain.performer.exception.AlreadyPerformerException;
import team.startup.gwangjutalentfestival.domain.performer.exception.InvalidPerformerVerificationException;
import team.startup.gwangjutalentfestival.domain.performer.exception.PerformerVerificationAlreadyClaimedException;
import team.startup.gwangjutalentfestival.domain.performer.presentation.data.request.VerifyPerformerRequest;
import team.startup.gwangjutalentfestival.domain.performer.repository.PerformerVerificationRepository;
import team.startup.gwangjutalentfestival.domain.performer.service.VerifyPerformerService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class VerifyPerformerServiceImpl implements VerifyPerformerService {

    private final PerformerVerificationRepository performerVerificationRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public TokenResponse execute(VerifyPerformerRequest request) {
        PerformerVerificationEntity verification = performerVerificationRepository
                .findByCodeHashForUpdate(hash(request.code().trim()))
                .orElseThrow(InvalidPerformerVerificationException::new);
        LocalDateTime now = LocalDateTime.now();

        if (!verification.getParticipantName().equals(request.name().trim()) || verification.isExpired(now)) {
            throw new InvalidPerformerVerificationException();
        }
        if (verification.isClaimed()) {
            throw new PerformerVerificationAlreadyClaimedException();
        }

        UserEntity user = userRepository.findByIdForUpdate(UserUtil.getCurrentUserId())
                .orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.USER) {
            throw new AlreadyPerformerException();
        }

        verification.claim(user.getId(), now);
        user.promoteToPerformer();

        TokenResponse token = jwtProvider.receiveToken(user.getId(), user.getRole());
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId().toString())
                .token(token.refreshToken())
                .expiresIn(jwtProperties.getRefreshTokenExpiration())
                .build());
        return token;
    }

    private String hash(String code) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
