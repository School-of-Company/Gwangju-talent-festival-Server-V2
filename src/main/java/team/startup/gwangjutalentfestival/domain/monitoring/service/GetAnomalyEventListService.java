package team.startup.gwangjutalentfestival.domain.monitoring.service;

import org.springframework.data.domain.Page;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventListResponse;

public interface GetAnomalyEventListService {

    Page<AnomalyEventListResponse> execute(int page, int size, AnomalyEventStatus status);
}
