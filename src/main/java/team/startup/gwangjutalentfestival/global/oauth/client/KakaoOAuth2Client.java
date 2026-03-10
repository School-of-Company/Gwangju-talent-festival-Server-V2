package team.startup.gwangjutalentfestival.global.oauth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import team.startup.gwangjutalentfestival.global.config.FeignConfig;
import team.startup.gwangjutalentfestival.global.oauth.data.KakaoTokenResponse;

import java.util.Map;

@FeignClient(name = "kakao-oauth2-client", url = "https://kauth.kakao.com", configuration = FeignConfig.class)
public interface KakaoOAuth2Client {

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoTokenResponse exchangeCodeForToken(@RequestBody Map<String, ?> formParams);
}