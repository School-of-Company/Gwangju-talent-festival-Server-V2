package team.startup.gwangjutalentfestival.domain.monitoring.service;

import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.CreateIncidentFeedbackRequest;

public interface CreateIncidentFeedbackService {

    void execute(Long anomalyEventId, CreateIncidentFeedbackRequest request);
}
