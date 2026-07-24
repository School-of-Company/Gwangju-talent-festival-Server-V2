package team.startup.gwangjutalentfestival.domain.judge.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgeCommentRequest;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgementScoreRequest;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeCommentResponse;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeEventService;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeMonitoringService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetAllJudgementService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeCommentService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgementService;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgeCommentService;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgementScoreService;

import java.util.List;

/**
 * 심사 관련 API 엔드포인트를 제공하는 컨트롤러.
 * SSE 연결, 점수 저장, 단일/전체 심사 조회 기능을 담당한다.
 */
@Tag(name = "Judge", description = "심사 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/judge")
public class JudgeController {

    private final SaveJudgementScoreService saveJudgementScoreService;
    private final GetAllJudgementService getAllJudgementService;
    private final GetJudgementService getJudgementService;
    private final ConnectSseJudgeEventService connectSseJudgeEventService;
    private final ConnectSseJudgeMonitoringService connectSseJudgeMonitoringService;
    private final GetJudgeCommentService getJudgeCommentService;
    private final SaveJudgeCommentService saveJudgeCommentService;

    @Operation(summary = "SSE 연결", description = "심사 이벤트 수신을 위한 SSE 연결을 맺습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공")
    })
    /**
     * 심사 이벤트 수신을 위한 SSE 연결을 맺는다.
     *
     * @return SSE 연결 객체
     */
    @SecurityRequirement(name = "Authorization")
    @GetMapping(value = "/changes", produces = "text/event-stream")
    public SseEmitter connect() {
        return connectSseJudgeEventService.execute();
    }

    @SecurityRequirement(name = "Authorization")
    @GetMapping(value = "/monitor/changes", produces = "text/event-stream")
    public SseEmitter connectMonitoring() {
        return connectSseJudgeMonitoringService.execute();
    }

    @Operation(summary = "심사 점수 저장", description = "팀에 대한 심사 점수를 저장하거나 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "점수 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 점수 입력"),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    /**
     * 특정 팀에 대한 심사 점수를 저장하거나 수정한다.
     *
     * @param teamId  대상 팀 ID
     * @param request 심사 점수 요청 데이터
     * @return 204 No Content
     */
    @SecurityRequirement(name = "Authorization")
    @PatchMapping("/{teamId}")
    public ResponseEntity<Void> saveJudgementScore(
            @PathVariable Long teamId,
            @RequestBody @Valid SaveJudgementScoreRequest request) {
        saveJudgementScoreService.execute(request, teamId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 팀 심사 조회", description = "모든 팀에 대한 현재 심사위원의 점수 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    /**
     * 현재 로그인한 심사위원의 전체 팀 심사 목록을 조회한다.
     *
     * @return 전체 팀 심사 응답 목록
     */
    @SecurityRequirement(name = "Authorization")
    @GetMapping
    public ResponseEntity<List<GetJudgementResponse>> getAllJudgement() {
        return ResponseEntity.ok(getAllJudgementService.execute());
    }

    @Operation(summary = "단일 팀 심사 조회", description = "특정 팀에 대한 현재 심사위원의 점수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    /**
     * 현재 로그인한 심사위원의 특정 팀 심사를 조회한다.
     *
     * @param teamId 조회할 팀 ID
     * @return 단일 팀 심사 응답
     */
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{teamId}")
    public ResponseEntity<GetJudgementResponse> getJudgement(
            @Parameter(description = "팀 ID", required = true) @PathVariable Long teamId) {
        return ResponseEntity.ok(getJudgementService.execute(teamId));
    }

    @Operation(summary = "필기 코멘트 조회", description = "특정 팀에 대한 현재 심사위원의 필기 코멘트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    /**
     * 현재 로그인한 심사위원의 특정 팀 필기 코멘트를 조회한다.
     *
     * @param teamId 조회할 팀 ID
     * @return 필기 코멘트 응답
     */
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{teamId}/comment")
    public ResponseEntity<GetJudgeCommentResponse> getJudgeComment(
            @Parameter(description = "팀 ID", required = true) @PathVariable Long teamId) {
        return ResponseEntity.ok(getJudgeCommentService.execute(teamId));
    }

    @Operation(summary = "필기 코멘트 저장", description = "특정 팀에 대한 필기 코멘트를 저장하거나 덮어씁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    /**
     * 특정 팀에 대한 필기 코멘트를 저장하거나 덮어쓴다.
     *
     * @param teamId  대상 팀 ID
     * @param request 필기 코멘트 요청 데이터
     * @return 204 No Content
     */
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/{teamId}/comment")
    public ResponseEntity<Void> saveJudgeComment(
            @PathVariable Long teamId,
            @RequestBody @Valid SaveJudgeCommentRequest request) {
        saveJudgeCommentService.execute(request, teamId);
        return ResponseEntity.noContent().build();
    }
}
