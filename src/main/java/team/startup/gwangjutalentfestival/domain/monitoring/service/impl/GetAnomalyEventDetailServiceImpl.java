package team.startup.gwangjutalentfestival.domain.monitoring.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.IncidentFeedbackEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.AnomalyEventNotFoundException;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventDetailResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.IncidentFeedbackResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventDetailService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetAnomalyEventDetailServiceImpl implements GetAnomalyEventDetailService {

    private final AnomalyEventRepository anomalyEventRepository;
    private final IncidentFeedbackRepository incidentFeedbackRepository;

    @Transactional(readOnly = true)
    @Override
    public AnomalyEventDetailResponse execute(Long id) {
        AnomalyEventEntity event = anomalyEventRepository.findById(id)
                .orElseThrow(AnomalyEventNotFoundException::new);

        Optional<IncidentFeedbackEntity> feedbackOpt = incidentFeedbackRepository.findByAnomalyEventId(id);

        IncidentFeedbackResponse feedbackResponse = feedbackOpt.map(f -> new IncidentFeedbackResponse(
                f.getId(), f.getLabel(), f.getNote(), f.getCreatedAt()
        )).orElse(null);

        return new AnomalyEventDetailResponse(
                event.getId(), event.getDomain(), event.getMetricName(), event.getStatus(),
                event.getDetectedValue(), event.getThresholdValue(), event.getReason(),
                event.getCreatedAt(), event.getResolvedAt(), feedbackResponse
        );
    }
}
