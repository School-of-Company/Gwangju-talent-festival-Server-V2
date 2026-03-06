package team.startup.gwangjutalentfestival.auth.service;

import team.startup.gwangjutalentfestival.auth.presentation.data.response.TokenResponse;

public interface ReissueTokenService {
    TokenResponse execute(String refreshToken);
}
