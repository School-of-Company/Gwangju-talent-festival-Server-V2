package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SendVerifyCodeRequest;

public interface SendVerifyCodeService {
    void execute(SendVerifyCodeRequest request);
}
