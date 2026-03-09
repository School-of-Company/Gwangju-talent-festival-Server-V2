package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.OAuthLoginRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;

public interface KaKaoOAuthLoginService {
    TokenResponse execute(OAuthLoginRequest request);
}
