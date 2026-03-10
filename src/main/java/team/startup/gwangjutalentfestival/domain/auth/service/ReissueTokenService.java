package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;

public interface ReissueTokenService {
    TokenResponse execute(String refreshToken);
}
