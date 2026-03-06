package team.startup.gwangjutalentfestival.global.oauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.data.ProviderProperties;

import java.util.HashMap;
import java.util.Map;

@Component
public class OAuthProviderConfig {

    private final Map<OAuthType, ProviderProperties> providers;

    public OAuthProviderConfig(
            @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String kakaoClientId,
            @Value("${spring.security.oauth2.client.registration.kakao.client-secret}") String kakaoClientSecret,
            @Value("${spring.security.oauth2.client.provider.kakao.token-uri}") String tokenUri,
            @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}") String userInfoUri
    ) {

        providers = new HashMap<>();
        providers.put(OAuthType.KAKAO,new ProviderProperties(
                kakaoClientId, kakaoClientSecret, tokenUri, userInfoUri
                )
        );
    }
public ProviderProperties getProvider(OAuthType type) {

        ProviderProperties providerProperties = providers.get(type);

        if (providerProperties == null) {
            throw new UnsupportedOperationException();
        }
        return providerProperties;
}


}
