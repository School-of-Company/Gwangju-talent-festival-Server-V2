package team.startup.gwangjutalentfestival.domain.monitoring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyEventRepository extends JpaRepository<AnomalyEventEntity, Long> {

    boolean existsByDomainAndMetricNameAndStatus(
            String domain, String metricName, AnomalyEventStatus status
    );

    Page<AnomalyEventEntity> findAll(Pageable pageable);

    Page<AnomalyEventEntity> findAllByStatus(AnomalyEventStatus status, Pageable pageable);

    long countByStatus(AnomalyEventStatus status);

    @Query("""
            SELECT e FROM AnomalyEventEntity e
            WHERE e.createdAt BETWEEN :start AND :end
            AND (:domain IS NULL OR e.domain = :domain)
            AND (:metricName IS NULL OR e.metricName = :metricName)
            """)
    List<AnomalyEventEntity> findForDataset(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("domain") String domain,
            @Param("metricName") String metricName
    );
}
