package team.startup.gwangjutalentfestival.domain.monitoring.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusRangePoint;
import team.startup.gwangjutalentfestival.domain.monitoring.detector.DatasetMetricQuery;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.IncidentFeedbackEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportFailedException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportInvalidFilterException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportInvalidPeriodException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportPeriodExceededException;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.ExportDatasetRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.ExportDatasetResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.DatasetProperties;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.ExportDatasetService;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportDatasetServiceImpl implements ExportDatasetService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+09:00");
    private static final int DEFAULT_STEP_SECONDS = 60;
    private static final int MAX_EXPORT_DAYS = 7;
    private static final Set<String> ALLOWED_DOMAINS = Set.of("seat", "judge");
    private static final Set<String> ALLOWED_METRIC_NAMES = Set.of("failure_rate", "p95_duration");

    private final PrometheusClient prometheusClient;
    private final AnomalyEventRepository anomalyEventRepository;
    private final IncidentFeedbackRepository incidentFeedbackRepository;
    private final DatasetProperties datasetProperties;

    @Override
    public ExportDatasetResponse execute(ExportDatasetRequest request) {
        int step = request.stepSeconds() != null ? request.stepSeconds() : DEFAULT_STEP_SECONDS;

        validatePeriod(request.start(), request.end());
        validateFilter(request.domain(), request.metricName());

        List<DatasetMetricQuery> queries = resolveQueries(request.domain(), request.metricName());
        if (queries.isEmpty()) {
            throw new ExportInvalidFilterException();
        }

        Path exportDir = prepareExportDir();
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String fileName = "dataset_" + LocalDateTime.now(ZONE).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + uniqueId + ".csv";
        Path filePath = exportDir.resolve(fileName).normalize();
        if (!filePath.startsWith(exportDir)) {
            throw new ExportFailedException();
        }

        Map<String, String> labelMap = new HashMap<>();
        Set<String> excludeSet = new HashSet<>();
        buildLabelMapping(request.start(), request.end(), request.domain(), request.metricName(), step, labelMap, excludeSet);

        long startEpoch = request.start().toEpochSecond(ZONE_OFFSET);
        long endEpoch = request.end().toEpochSecond(ZONE_OFFSET);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("domain", "metricName", "timestamp", "value", "hourOfDay", "dayOfWeek", "label")
                .build();

        long rowCount = 0;
        try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (DatasetMetricQuery query : queries) {
                List<PrometheusRangePoint> rawPoints = prometheusClient.queryRange(query.getPromql(), startEpoch, endEpoch, step);
                if (rawPoints.isEmpty()) {
                    log.warn("Prometheus queryRange 결과 없음. domain={}, metricName={}", query.getDomain(), query.getMetricName());
                    continue;
                }

                // 동일 timestamp 중복 시 첫 번째 정상 값 사용 (여러 series가 반환될 경우 대비)
                LinkedHashMap<Long, Double> deduped = new LinkedHashMap<>();
                for (PrometheusRangePoint point : rawPoints) {
                    deduped.putIfAbsent(point.timestamp(), point.value());
                }

                for (Map.Entry<Long, Double> entry : deduped.entrySet()) {
                    long ts = entry.getKey();
                    double value = entry.getValue();
                    String key = query.getDomain() + ":" + query.getMetricName() + ":" + ts;

                    if (excludeSet.contains(key)) {
                        continue;
                    }

                    String label = labelMap.getOrDefault(key, "normal");
                    LocalDateTime timestamp = LocalDateTime.ofEpochSecond(ts, 0, ZONE_OFFSET);

                    printer.printRecord(
                            query.getDomain(),
                            query.getMetricName(),
                            timestamp.toString(),
                            value,
                            timestamp.getHour(),
                            timestamp.getDayOfWeek().name(),
                            label
                    );
                    rowCount++;
                }
            }
        } catch (IOException e) {
            log.error("CSV 파일 작성 실패. path={}", filePath, e);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException deleteEx) {
                log.error("불완전한 CSV 파일 삭제 실패. path={}", filePath, deleteEx);
            }
            throw new ExportFailedException();
        }

        log.info("데이터셋 export 완료. path={}, rowCount={}", filePath, rowCount);
        return new ExportDatasetResponse(filePath.toString(), rowCount);
    }

    private void validatePeriod(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ExportInvalidPeriodException();
        }
        if (Duration.between(start, end).compareTo(Duration.ofDays(MAX_EXPORT_DAYS)) > 0) {
            throw new ExportPeriodExceededException();
        }
    }

    private void validateFilter(String domain, String metricName) {
        if (domain != null && !ALLOWED_DOMAINS.contains(domain)) {
            throw new ExportInvalidFilterException();
        }
        if (metricName != null && !ALLOWED_METRIC_NAMES.contains(metricName)) {
            throw new ExportInvalidFilterException();
        }
    }

    private List<DatasetMetricQuery> resolveQueries(String domain, String metricName) {
        return DatasetMetricQuery.ALL.stream()
                .filter(q -> domain == null || q.getDomain().equals(domain))
                .filter(q -> metricName == null || q.getMetricName().equals(metricName))
                .collect(Collectors.toList());
    }

    private Path prepareExportDir() {
        Path exportDir = Paths.get(datasetProperties.exportPath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(exportDir);
        } catch (IOException e) {
            log.error("export 디렉터리 생성 실패. path={}", exportDir, e);
            throw new ExportFailedException();
        }
        return exportDir;
    }

    private void buildLabelMapping(
            LocalDateTime start, LocalDateTime end,
            String domain, String metricName,
            int step,
            Map<String, String> labelMap,
            Set<String> excludeSet
    ) {
        List<AnomalyEventEntity> events = anomalyEventRepository.findForDataset(start, end, domain, metricName);
        if (events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream().map(AnomalyEventEntity::getId).collect(Collectors.toList());
        Map<Long, IncidentFeedbackEntity> feedbackMap = incidentFeedbackRepository
                .findAllByAnomalyEvent_IdIn(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getAnomalyEvent().getId(),
                        f -> f,
                        (existing, replacement) -> existing
                ));

        long startEpoch = start.toEpochSecond(ZONE_OFFSET);
        for (AnomalyEventEntity event : events) {
            long floorEpoch = startEpoch + ((event.getCreatedAt().toEpochSecond(ZONE_OFFSET) - startEpoch) / step) * step;
            String key = event.getDomain() + ":" + event.getMetricName() + ":" + floorEpoch;

            IncidentFeedbackEntity feedback = feedbackMap.get(event.getId());
            if (feedback == null) {
                // feedback 없는 OPEN 이벤트 — 학습 데이터에서 제외
                excludeSet.add(key);
            } else if (feedback.getLabel() == FeedbackLabel.TRUE_INCIDENT) {
                labelMap.put(key, "anomaly");
            } else if (feedback.getLabel() == FeedbackLabel.FALSE_POSITIVE) {
                labelMap.put(key, "normal");
            } else if (feedback.getLabel() == FeedbackLabel.IGNORED) {
                // IGNORED — 학습 데이터에서 제외
                excludeSet.add(key);
            }
        }
    }
}
