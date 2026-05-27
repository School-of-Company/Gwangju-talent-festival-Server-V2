package team.startup.gwangjutalentfestival.domain.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;

public interface AnomalyEventRepository extends JpaRepository<AnomalyEventEntity, Long> {

    boolean existsByDomainAndMetricNameAndStatus(
            String domain, String metricName, AnomalyEventStatus status
    );
}
