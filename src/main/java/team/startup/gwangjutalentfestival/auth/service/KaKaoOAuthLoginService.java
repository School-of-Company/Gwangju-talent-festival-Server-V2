package team.startup.gwangjutalentfestival.auth.service;

import team.startup.gwangjutalentfestival.auth.presentation.data.request.OAuthLoginRequest;
import team.startup.gwangjutalentfestival.auth.presentation.data.response.TokenResponse;

public interface KaKaoOAuthLoginService {
    TokenResponse execute(OAuthLoginRequest request);
}
