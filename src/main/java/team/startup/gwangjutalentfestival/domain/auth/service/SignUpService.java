package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SignUpRequest;

public interface SignUpService {
    void execute(SignUpRequest request);
}
