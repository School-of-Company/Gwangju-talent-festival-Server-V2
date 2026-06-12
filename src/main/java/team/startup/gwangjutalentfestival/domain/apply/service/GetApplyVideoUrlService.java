package team.startup.gwangjutalentfestival.domain.apply.service;

import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyVideoUrlResponse;

public interface GetApplyVideoUrlService {
    ApplyVideoUrlResponse execute(Long applyId);
}
