package team.startup.gwangjutalentfestival.domain.monitoring.service;

import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventDetailResponse;

public interface GetAnomalyEventDetailService {

    AnomalyEventDetailResponse execute(Long id);
}
