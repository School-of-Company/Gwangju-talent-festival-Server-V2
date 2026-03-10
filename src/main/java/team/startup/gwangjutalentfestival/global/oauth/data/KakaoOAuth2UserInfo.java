package team.startup.gwangjutalentfestival.global.oauth.data;

import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.dto.OAuthUserResponse;
import team.startup.gwangjutalentfestival.global.oauth.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;
import java.util.Optional;

public record KakaoOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public OAuthUserResponse toResponse() {
        String id = Optional.ofNullable(attributes.get("id"))
                .map(Object::toString)
                .orElseThrow(OAuth2AuthenticationProcessingException::new);

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) Optional.ofNullable(attributes.get("kakao_account"))
                .orElseThrow(OAuth2AuthenticationProcessingException::new);

        String email = Optional.ofNullable(kakaoAccount.get("email"))
                .map(Object::toString)
                .orElseThrow(OAuth2AuthenticationProcessingException::new);

        return new OAuthUserResponse(id, OAuthType.KAKAO, email, Role.USER);
    }
}
