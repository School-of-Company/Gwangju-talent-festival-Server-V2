package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.LoginRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;

public interface LoginService {
    TokenResponse execute(LoginRequest loginRequest);
}
