package team.startup.gwangjutalentfestival.domain.monitoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusRangePoint;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.IncidentFeedbackEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportInvalidFilterException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportInvalidPeriodException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.ExportPeriodExceededException;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.ExportDatasetRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.ExportDatasetResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.DatasetProperties;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.impl.ExportDatasetServiceImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ExportDatasetServiceTest {

    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+09:00");
    private static final LocalDateTime START = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 6, 1, 1, 0, 0);
    // prometheus 포인트 timestamp: START epoch (step=60 정렬)
    private static final long POINT_TS = (START.toEpochSecond(ZONE_OFFSET) / 60) * 60;
    // createdAt: POINT_TS + 30초 내에 위치 → floorEpoch == POINT_TS
    private static final LocalDateTime EVENT_CREATED_AT = LocalDateTime.ofEpochSecond(POINT_TS + 30, 0, ZONE_OFFSET);

    @Mock
    private PrometheusClient prometheusClient;
    @Mock
    private AnomalyEventRepository anomalyEventRepository;
    @Mock
    private IncidentFeedbackRepository incidentFeedbackRepository;
    @Mock
    private DatasetProperties datasetProperties;

    @InjectMocks
    private ExportDatasetServiceImpl exportDatasetService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        lenient().when(datasetProperties.exportPath()).thenReturn(tempDir.toString());
    }

    @Test
    void anomaly_event가_없는_구간은_normal로_CSV에_포함된다() {
        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of());
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of(new PrometheusRangePoint(POINT_TS, 0.012)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", "failure_rate")
        );

        assertThat(response.rowCount()).isEqualTo(1);
        assertThat(Files.exists(Path.of(response.filePath()))).isTrue();
    }

    @Test
    void TRUE_INCIDENT_피드백_포인트는_anomaly_label로_CSV에_포함된다() throws Exception {
        AnomalyEventEntity event = buildEvent(1L, "seat", "failure_rate", AnomalyEventStatus.RESOLVED,
                EVENT_CREATED_AT);
        IncidentFeedbackEntity feedback = buildFeedback(event, FeedbackLabel.TRUE_INCIDENT);

        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of(event));
        given(incidentFeedbackRepository.findAllByAnomalyEvent_IdIn(any())).willReturn(List.of(feedback));
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of(new PrometheusRangePoint(POINT_TS, 0.087)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", "failure_rate")
        );

        assertThat(response.rowCount()).isEqualTo(1);
        String content = Files.readString(Path.of(response.filePath()));
        assertThat(content).contains("anomaly");
    }

    @Test
    void FALSE_POSITIVE_피드백_포인트는_normal_label로_CSV에_포함된다() throws Exception {
        AnomalyEventEntity event = buildEvent(1L, "seat", "failure_rate", AnomalyEventStatus.RESOLVED,
                EVENT_CREATED_AT);
        IncidentFeedbackEntity feedback = buildFeedback(event, FeedbackLabel.FALSE_POSITIVE);

        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of(event));
        given(incidentFeedbackRepository.findAllByAnomalyEvent_IdIn(any())).willReturn(List.of(feedback));
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of(new PrometheusRangePoint(POINT_TS, 0.087)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", "failure_rate")
        );

        assertThat(response.rowCount()).isEqualTo(1);
        String content = Files.readString(Path.of(response.filePath()));
        assertThat(content).doesNotContain("anomaly");
        assertThat(content).contains("normal");
    }

    @Test
    void IGNORED_피드백_포인트는_CSV에_포함되지_않는다() {
        AnomalyEventEntity event = buildEvent(1L, "seat", "failure_rate", AnomalyEventStatus.IGNORED,
                EVENT_CREATED_AT);
        IncidentFeedbackEntity feedback = buildFeedback(event, FeedbackLabel.IGNORED);

        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of(event));
        given(incidentFeedbackRepository.findAllByAnomalyEvent_IdIn(any())).willReturn(List.of(feedback));
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of(new PrometheusRangePoint(POINT_TS, 0.087)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", "failure_rate")
        );

        assertThat(response.rowCount()).isEqualTo(0);
    }

    @Test
    void feedback_없는_OPEN_이벤트_포인트는_CSV에_포함되지_않는다() {
        AnomalyEventEntity event = buildEvent(1L, "seat", "failure_rate", AnomalyEventStatus.OPEN,
                EVENT_CREATED_AT);

        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of(event));
        given(incidentFeedbackRepository.findAllByAnomalyEvent_IdIn(any())).willReturn(List.of());
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of(new PrometheusRangePoint(POINT_TS, 0.087)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", "failure_rate")
        );

        assertThat(response.rowCount()).isEqualTo(0);
    }

    @Test
    void Prometheus_queryRange_실패_시_해당_메트릭_skip_후_나머지_정상_처리된다() {
        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of());
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of())
                .willReturn(List.of(new PrometheusRangePoint(POINT_TS, 1.5)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", null)
        );

        assertThat(response.rowCount()).isEqualTo(1);
    }

    @Test
    void CSV_파일이_실제로_생성된다() {
        given(anomalyEventRepository.findForDataset(any(), any(), any(), any())).willReturn(List.of());
        given(prometheusClient.queryRange(anyString(), anyLong(), anyLong(), anyInt()))
                .willReturn(List.of(new PrometheusRangePoint(1748736000L, 0.01)));

        ExportDatasetResponse response = exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "seat", "failure_rate")
        );

        assertThat(Files.exists(Path.of(response.filePath()))).isTrue();
        assertThat(response.filePath()).endsWith(".csv");
    }

    @Test
    void end가_start보다_이전이면_ExportInvalidPeriodException이_발생한다() {
        assertThatThrownBy(() -> exportDatasetService.execute(
                new ExportDatasetRequest(END, START, 60, null, null)
        )).isInstanceOf(ExportInvalidPeriodException.class);
    }

    @Test
    void end가_start와_같으면_ExportInvalidPeriodException이_발생한다() {
        assertThatThrownBy(() -> exportDatasetService.execute(
                new ExportDatasetRequest(START, START, 60, null, null)
        )).isInstanceOf(ExportInvalidPeriodException.class);
    }

    @Test
    void 기간이_7일_초과면_ExportPeriodExceededException이_발생한다() {
        assertThatThrownBy(() -> exportDatasetService.execute(
                new ExportDatasetRequest(START, START.plusDays(8), 60, null, null)
        )).isInstanceOf(ExportPeriodExceededException.class);
    }

    @Test
    void 허용되지_않는_domain이면_ExportInvalidFilterException이_발생한다() {
        assertThatThrownBy(() -> exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, "unknown", null)
        )).isInstanceOf(ExportInvalidFilterException.class);
    }

    @Test
    void 허용되지_않는_metricName이면_ExportInvalidFilterException이_발생한다() {
        assertThatThrownBy(() -> exportDatasetService.execute(
                new ExportDatasetRequest(START, END, 60, null, "unknown_metric")
        )).isInstanceOf(ExportInvalidFilterException.class);
    }

    private AnomalyEventEntity buildEvent(Long id, String domain, String metricName,
                                           AnomalyEventStatus status, LocalDateTime createdAt) {
        return AnomalyEventEntity.builder()
                .id(id)
                .domain(domain)
                .metricName(metricName)
                .status(status)
                .detectedValue(0.087)
                .thresholdValue(0.05)
                .reason("테스트")
                .createdAt(createdAt)
                .build();
    }

    private IncidentFeedbackEntity buildFeedback(AnomalyEventEntity event, FeedbackLabel label) {
        return IncidentFeedbackEntity.builder()
                .id(1L)
                .anomalyEvent(event)
                .label(label)
                .build();
    }
}