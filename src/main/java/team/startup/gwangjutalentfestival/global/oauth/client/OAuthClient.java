package team.startup.gwangjutalentfestival.global.oauth.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.config.OAuthProviderConfig;
import team.startup.gwangjutalentfestival.global.oauth.data.KakaoTokenResponse;
import team.startup.gwangjutalentfestival.global.oauth.data.ProviderProperties;
import team.startup.gwangjutalentfestival.global.oauth.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuthClient {

    private final RestClient restClient;
    private final OAuthProviderConfig oAuthProviderConfig;
    private final KakaoOAuth2Client kakaoOAuth2Client;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    public String getAccessToken(OAuthType type, String code, String redirectUri) {
        ProviderProperties properties = oAuthProviderConfig.getProvider(type);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", properties.clientId());
        params.add("client_secret", properties.clientSecret());
        params.add("redirect_uri", redirectUri);

        try {
            KakaoTokenResponse response = kakaoOAuth2Client.exchangeCodeForToken(params);
            return Optional.ofNullable(response.accessToken())
                    .filter(token -> !token.isBlank())
                    .orElseThrow(OAuth2AuthenticationProcessingException::new);
        }catch (Exception e){
            throw new OAuth2AuthenticationProcessingException();
        }
    }

    public Map<String, Object> getUserAttributes(OAuthType type, String accessToken) {
        ProviderProperties properties = oAuthProviderConfig.getProvider(type);

        try {
            Map<String, Object> attributes = restClient.get()
                    .uri(properties.userInfoUri())
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .body(MAP_TYPE);

            return Optional.ofNullable(attributes)
                    .orElseThrow(OAuth2AuthenticationProcessingException::new);
        } catch (RestClientException e) {
            throw new OAuth2AuthenticationProcessingException();
        }
    }
}
