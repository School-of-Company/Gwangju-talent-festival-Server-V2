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

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

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
