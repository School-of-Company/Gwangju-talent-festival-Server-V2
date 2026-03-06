package team.startup.gwangjutalentfestival.global.oauth.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.config.OAuthProviderConfig;
import team.startup.gwangjutalentfestival.global.oauth.data.ProviderProperties;
import team.startup.gwangjutalentfestival.global.oauth.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthClient {

    private final RestClient restTemplate;
    private final OAuthProviderConfig oAuthProviderConfig;

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

        log.info("[OAuth] 액세스 토큰 요청 - provider: {}, redirectUri: {}", type, redirectUri);
        try {
            Map<String, Object> response = restTemplate.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(MAP_TYPE);

            log.info("[OAuth] 카카오 토큰 응답: {}", response);
            return Optional.ofNullable(response)
                    .map(r -> (String)r.get("access_token"))
                    .filter(token -> !token.isBlank())
                    .orElseThrow(OAuth2AuthenticationProcessingException::new);
        } catch (RestClientException e) {
            log.error("[OAuth] 액세스 토큰 요청 실패: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationProcessingException();
        }
    }

    public Map<String, Object> getUserAttributes(OAuthType type, String accessToken) {
        ProviderProperties properties = oAuthProviderConfig.getProvider(type);

        log.info("[OAuth] 유저 정보 요청 - provider: {}", type);
        try {
            Map<String, Object> attributes = restTemplate.get()
                    .uri(properties.userInfoUri())
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .body(MAP_TYPE);

            log.info("[OAuth] 유저 정보 응답: {}", attributes);
            return Optional.ofNullable(attributes)
                    .orElseThrow(OAuth2AuthenticationProcessingException::new);
        } catch (RestClientException e) {
            log.error("[OAuth] 유저 정보 요청 실패: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationProcessingException();
        }
    }
}
