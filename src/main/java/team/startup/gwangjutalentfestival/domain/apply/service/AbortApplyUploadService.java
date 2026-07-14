package team.startup.gwangjutalentfestival.domain.apply.service;

import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyAbortRequest;

public interface AbortApplyUploadService {
    void execute(ApplyAbortRequest request);
}
