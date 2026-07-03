package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidPasswordException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.LoginRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.LoginService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

/**
 * {@link LoginService} 구현체.
 * <p>비밀번호를 검증하고 JWT 토큰을 발급한 뒤 RefreshToken을 Redis에 저장합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * 휴대폰 번호로 사용자를 조회하고 비밀번호를 검증한 뒤 JWT 토큰을 발급합니다.
     *
     * @param request 로그인 요청 정보 (휴대폰 번호, 비밀번호)
     * @return 발급된 AccessToken 및 RefreshToken 정보
     * @throws team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException 존재하지 않는 사용자인 경우
     * @throws InvalidPasswordException 비밀번호가 일치하지 않는 경우
     */
    @Override
    @Transactional
    public TokenResponse execute(LoginRequest request) {
        UserEntity user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        TokenResponse token = jwtProvider.receiveToken(user.getId(), user.getRole());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(String.valueOf(user.getId()))
                .token(token.refreshToken())
                .expiresIn(jwtProperties.getRefreshTokenExpiration())
                .build());

        return token;
    }
}
