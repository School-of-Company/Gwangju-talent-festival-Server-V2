package team.startup.gwangjutalentfestival.domain.monitoring.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventSummaryResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventSummaryService;

@Service
@RequiredArgsConstructor
public class GetAnomalyEventSummaryServiceImpl implements GetAnomalyEventSummaryService {

    private final AnomalyEventRepository anomalyEventRepository;
    private final IncidentFeedbackRepository incidentFeedbackRepository;

    @Transactional(readOnly = true)
    @Override
    public AnomalyEventSummaryResponse execute() {
        long totalEvents = anomalyEventRepository.count();
        long openEvents = anomalyEventRepository.countByStatus(AnomalyEventStatus.OPEN);
        long resolvedEvents = anomalyEventRepository.countByStatus(AnomalyEventStatus.RESOLVED);
        long ignoredEvents = anomalyEventRepository.countByStatus(AnomalyEventStatus.IGNORED);
        long trueIncidentCount = incidentFeedbackRepository.countByLabel(FeedbackLabel.TRUE_INCIDENT);
        long falsePositiveCount = incidentFeedbackRepository.countByLabel(FeedbackLabel.FALSE_POSITIVE);

        long denominator = trueIncidentCount + falsePositiveCount;
        double falsePositiveRate = denominator > 0 ? (double) falsePositiveCount / denominator : 0.0;

        return new AnomalyEventSummaryResponse(
                totalEvents, openEvents, resolvedEvents, ignoredEvents,
                trueIncidentCount, falsePositiveCount, falsePositiveRate
        );
    }
}
