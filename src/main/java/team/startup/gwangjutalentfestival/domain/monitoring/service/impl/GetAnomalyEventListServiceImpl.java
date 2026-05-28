package team.startup.gwangjutalentfestival.domain.monitoring.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventListResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventListService;

@Service
@RequiredArgsConstructor
public class GetAnomalyEventListServiceImpl implements GetAnomalyEventListService {

    private final AnomalyEventRepository anomalyEventRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<AnomalyEventListResponse> execute(int page, int size, AnomalyEventStatus status) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());

        Page<AnomalyEventEntity> events = (status == null)
                ? anomalyEventRepository.findAll(pageRequest)
                : anomalyEventRepository.findAllByStatus(status, pageRequest);

        return events.map(e -> new AnomalyEventListResponse(
                e.getId(), e.getDomain(), e.getMetricName(), e.getStatus(),
                e.getDetectedValue(), e.getThresholdValue(), e.getReason(),
                e.getCreatedAt(), e.getResolvedAt()
        ));
    }
}
