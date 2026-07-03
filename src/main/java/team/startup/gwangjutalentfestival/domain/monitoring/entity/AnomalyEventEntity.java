package team.startup.gwangjutalentfestival.domain.monitoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "anomaly_event")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AnomalyEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String domain;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnomalyEventStatus status;

    @Column(name = "detected_value", nullable = false)
    private Double detectedValue;

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Column(nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at", nullable = true)
    private LocalDateTime resolvedAt;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "predicted_label", length = 20)
    private String predictedLabel;

    public void resolve(LocalDateTime resolvedAt) {
        this.status = AnomalyEventStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
    }

    public void ignore(LocalDateTime resolvedAt) {
        this.status = AnomalyEventStatus.IGNORED;
        this.resolvedAt = resolvedAt;
    }
}
