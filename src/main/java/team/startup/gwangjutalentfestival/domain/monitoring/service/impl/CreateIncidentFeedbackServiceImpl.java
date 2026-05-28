package team.startup.gwangjutalentfestival.domain.monitoring.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.IncidentFeedbackEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.AnomalyEventNotFoundException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.FeedbackAlreadyExistsException;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.CreateIncidentFeedbackRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.CreateIncidentFeedbackService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateIncidentFeedbackServiceImpl implements CreateIncidentFeedbackService {

    private final AnomalyEventRepository anomalyEventRepository;
    private final IncidentFeedbackRepository incidentFeedbackRepository;

    @Transactional
    @Override
    public void execute(Long anomalyEventId, CreateIncidentFeedbackRequest request) {
        AnomalyEventEntity event = anomalyEventRepository.findById(anomalyEventId)
                .orElseThrow(AnomalyEventNotFoundException::new);

        if (incidentFeedbackRepository.existsByAnomalyEventId(anomalyEventId)) {
            throw new FeedbackAlreadyExistsException();
        }

        try {
            incidentFeedbackRepository.save(
                    IncidentFeedbackEntity.builder()
                            .anomalyEvent(event)
                            .label(request.label())
                            .note(request.note())
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            throw new FeedbackAlreadyExistsException();
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.label() == FeedbackLabel.IGNORED) {
            event.ignore(now);
        } else {
            event.resolve(now);
        }
    }
}
