package team.startup.gwangjutalentfestival.global.oauth.factory;

import team.startup.gwangjutalentfestival.global.oauth.data.KakaoOAuth2UserInfo;
import team.startup.gwangjutalentfestival.global.oauth.data.OAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return new KakaoOAuth2UserInfo(attributes);
    }
}
