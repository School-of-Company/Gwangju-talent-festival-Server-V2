package team.startup.gwangjutalentfestival.global.oauth.factory;

import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.data.KakaoOAuth2UserInfo;
import team.startup.gwangjutalentfestival.global.oauth.data.OAuth2UserInfo;
import team.startup.gwangjutalentfestival.global.oauth.exception.OAuthProviderNotSupportedException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (OAuthType.KAKAO.name().equalsIgnoreCase(registrationId)) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        throw new OAuthProviderNotSupportedException();
    }
}
