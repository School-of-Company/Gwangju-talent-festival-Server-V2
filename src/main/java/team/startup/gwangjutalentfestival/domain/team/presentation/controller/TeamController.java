package team.startup.gwangjutalentfestival.domain.team.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.request.UpdateTeamOrderRequest;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.UploadTeamVideoResponse;
import team.startup.gwangjutalentfestival.domain.team.service.GetAllTeamService;
import team.startup.gwangjutalentfestival.domain.team.service.GetTeamRankingService;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamOrderService;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamPerformanceService;
import team.startup.gwangjutalentfestival.domain.team.service.UploadTeamVideoService;

import java.util.List;

/**
 * 팀 관련 REST API 컨트롤러.
 * 팀 조회, 랭킹 조회, 공연 상태 변경, 공연 순서 변경 엔드포인트를 제공한다.
 */
@Tag(name = "Team", description = "팀 API")
@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final GetAllTeamService getAllTeamService;
    private final GetTeamRankingService getTeamRankingService;
    private final UpdateTeamPerformanceService updateTeamPerformanceService;
    private final UpdateTeamOrderService updateTeamOrderService;
    private final UploadTeamVideoService uploadTeamVideoService;

    /**
     * 등록된 모든 팀을 공연 순서 오름차순으로 조회한다.
     *
     * @return 전체 팀 목록
     */
    @Operation(summary = "전체 팀 조회", description = "등록된 모든 팀의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 팀 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<GetTeamResponse>> getAllTeam() {
        return ResponseEntity.ok(getAllTeamService.execute());
    }

    /**
     * 총점 기준으로 팀 랭킹을 조회한다.
     *
     * @return 랭킹 순서로 정렬된 팀 목록
     */
    @Operation(summary = "팀 랭킹 조회", description = "팀의 랭킹 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 랭킹 조회 성공")
    })
    @GetMapping("/ranking")
    public ResponseEntity<List<GetTeamRankingResponse>> getTeamRanking() {
        return ResponseEntity.ok(getTeamRankingService.execute());
    }

    /**
     * 특정 팀의 공연 상태를 다음 단계로 변경한다.
     *
     * @param teamId 상태를 변경할 팀 ID
     * @return 204 No Content
     */
    @Operation(summary = "팀 공연 상태 변경", description = "팀의 공연 상태를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "공연 상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀")
    })
    @PatchMapping("/{teamId}/performance")
    public ResponseEntity<Void> updateTeamPerformance(
            @Parameter(description = "팀 ID", required = true)
            @PathVariable Long teamId) {
        updateTeamPerformanceService.execute(teamId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 여러 팀의 공연 순서를 일괄 변경한다.
     *
     * @param request 팀별 변경할 공연 순서 목록
     * @return 204 No Content
     */
    @Operation(summary = "팀 공연 순서 변경", description = "팀의 공연 순서를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "공연 순서 변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀")
    })
    @PatchMapping("/order")
    public ResponseEntity<Void> updateTeamOrder(@RequestBody UpdateTeamOrderRequest request) {
        updateTeamOrderService.execute(request.orderItems());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 영상 업로드", description = "팀 공연 영상(MP4)을 업로드하고 7일 유효 다운로드 링크를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영상 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 영상 파일"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 실패")
    })
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadTeamVideoResponse> uploadTeamVideo(
            @Parameter(description = "업로드할 MP4 파일", required = true)
            @RequestPart MultipartFile file) {
        return ResponseEntity.ok(uploadTeamVideoService.execute(file));
    }
}