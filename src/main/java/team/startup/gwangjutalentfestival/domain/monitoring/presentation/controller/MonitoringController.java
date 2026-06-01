package team.startup.gwangjutalentfestival.domain.monitoring.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.CreateIncidentFeedbackRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventDetailResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventListResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventSummaryResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.service.CreateIncidentFeedbackService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventDetailService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventListService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventSummaryService;

@Tag(name = "Monitoring", description = "이상 탐지 모니터링 API (ADMIN 전용)")
@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final GetAnomalyEventListService getAnomalyEventListService;
    private final GetAnomalyEventDetailService getAnomalyEventDetailService;
    private final CreateIncidentFeedbackService createIncidentFeedbackService;
    private final GetAnomalyEventSummaryService getAnomalyEventSummaryService;

    @Operation(summary = "이상 탐지 이벤트 목록 조회", description = "최신순으로 정렬된 이상 탐지 이벤트 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/anomalies")
    public ResponseEntity<Page<AnomalyEventListResponse>> getAnomalyEventList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AnomalyEventStatus status
    ) {
        return ResponseEntity.ok(getAnomalyEventListService.execute(page, size, status));
    }

    @Operation(summary = "이상 탐지 요약 조회", description = "전체 이벤트 수, 상태별 카운트, 오탐률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/anomalies/summary")
    public ResponseEntity<AnomalyEventSummaryResponse> getAnomalyEventSummary() {
        return ResponseEntity.ok(getAnomalyEventSummaryService.execute());
    }

    @Operation(summary = "이상 탐지 이벤트 단일 조회", description = "특정 이벤트의 상세 정보와 피드백을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/anomalies/{id}")
    public ResponseEntity<AnomalyEventDetailResponse> getAnomalyEventDetail(@PathVariable Long id) {
        return ResponseEntity.ok(getAnomalyEventDetailService.execute(id));
    }

    @Operation(summary = "이상 탐지 피드백 등록", description = "이벤트에 피드백을 등록하고 상태를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "피드백 등록 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 피드백이 존재함")
    })
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/anomalies/{id}/feedback")
    public ResponseEntity<Void> createIncidentFeedback(
            @PathVariable Long id,
            @Valid @RequestBody CreateIncidentFeedbackRequest request
    ) {
        createIncidentFeedbackService.execute(id, request);
        return ResponseEntity.noContent().build();
    }
}
