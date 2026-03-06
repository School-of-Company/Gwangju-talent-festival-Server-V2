package team.startup.gwangjutalentfestival.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.auth.entity.RefreshToken;
import team.startup.gwangjutalentfestival.auth.presentation.data.request.OAuthLoginRequest;
import team.startup.gwangjutalentfestival.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.auth.repository.RefreshTokenRepository;
import team.startup.gwangjutalentfestival.auth.service.KaKaoOAuthLoginService;
import team.startup.gwangjutalentfestival.auth.service.MemberRegistrationService;
import team.startup.gwangjutalentfestival.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;
import team.startup.gwangjutalentfestival.global.oauth.client.OAuthClient;
import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.data.MemberCommand;
import team.startup.gwangjutalentfestival.global.oauth.dto.OAuthUserResponse;
import team.startup.gwangjutalentfestival.global.oauth.exception.OAuth2AuthenticationProcessingException;
import team.startup.gwangjutalentfestival.global.oauth.factory.OAuth2UserInfoFactory;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class KaKaoOAuthLoginServiceImpl implements KaKaoOAuthLoginService {

    private final OAuthClient oAuthClient;
    private final MemberRegistrationService memberRegistrationService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Override
    public TokenResponse execute(OAuthLoginRequest request) {
        OAuthType type;

        try {
            type = OAuthType.valueOf(request.provider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationProcessingException();
        }
        String accessToken = oAuthClient.getAccessToken(type, request.code(), request.redirectUri());
        Map<String, Object> attributes = oAuthClient.getUserAttributes(type, accessToken);

        OAuthUserResponse userInfo = extractUserInfo(type, attributes);
        UserEntity user = memberRegistrationService.findOrRegister(MemberCommand.from(userInfo));

        TokenResponse response = jwtProvider.receiveToken(user.getId(), user.getRole());

        String userId = String.valueOf(user.getId());
        refreshTokenRepository.deleteById(userId);
        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(response.refreshToken())
                .expiresIn(jwtProperties.getRefreshTokenExpiration())
                .build();
        refreshTokenRepository.save(token);

        return response;
    }

    private OAuthUserResponse extractUserInfo(OAuthType type, Map<String, Object> attributes) {
        try {
            return OAuth2UserInfoFactory.getOAuth2UserInfo(type.name(), attributes).toResponse();
        } catch (Exception e) {
            throw new OAuth2AuthenticationProcessingException();
        }
    }
}
