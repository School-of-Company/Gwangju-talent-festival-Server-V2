package team.startup.gwangjutalentfestival.domain.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.IncidentFeedbackEntity;

import java.util.Optional;

public interface IncidentFeedbackRepository extends JpaRepository<IncidentFeedbackEntity, Long> {

    boolean existsByAnomalyEventId(Long anomalyEventId);

    Optional<IncidentFeedbackEntity> findByAnomalyEventId(Long anomalyEventId);

    long countByLabel(FeedbackLabel label);
}
