package team.startup.gwangjutalentfestival.domain.monitoring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;

public interface AnomalyEventRepository extends JpaRepository<AnomalyEventEntity, Long> {

    boolean existsByDomainAndMetricNameAndStatus(
            String domain, String metricName, AnomalyEventStatus status
    );

    Page<AnomalyEventEntity> findAll(Pageable pageable);

    Page<AnomalyEventEntity> findAllByStatus(AnomalyEventStatus status, Pageable pageable);

    long countByStatus(AnomalyEventStatus status);
}
