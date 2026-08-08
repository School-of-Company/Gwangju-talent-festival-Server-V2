package team.startup.gwangjutalentfestival.domain.performer.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.performer.presentation.data.request.VerifyPerformerRequest;

public interface VerifyPerformerService {
    TokenResponse execute(VerifyPerformerRequest request);
}
