package team.startup.gwangjutalentfestival.domain.apply.service;

import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyCompleteRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;

public interface ApplyService {
    ApplyResponse execute(ApplyCompleteRequest request);
}
