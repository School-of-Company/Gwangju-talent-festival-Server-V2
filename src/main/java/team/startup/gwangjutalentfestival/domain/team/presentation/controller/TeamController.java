package team.startup.gwangjutalentfestival.domain.team.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.request.UpdateTeamOrderRequest;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;
import team.startup.gwangjutalentfestival.domain.team.service.GetAllTeamService;
import team.startup.gwangjutalentfestival.domain.team.service.GetTeamRankingService;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamOrderService;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamPerformanceService;

import java.util.List;

@Tag(name = "Team", description = "팀 API")
@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final GetAllTeamService getAllTeamService;
    private final GetTeamRankingService getTeamRankingService;
    private final UpdateTeamPerformanceService updateTeamPerformanceService;
    private final UpdateTeamOrderService updateTeamOrderService;

    @Operation(summary = "전체 팀 조회", description = "등록된 모든 팀의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 팀 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<GetTeamResponse>> getAllTeam() {
        return ResponseEntity.ok(getAllTeamService.execute());
    }

    @Operation(summary = "팀 랭킹 조회", description = "팀의 랭킹 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 랭킹 조회 성공")
    })
    @GetMapping("/ranking")
    public ResponseEntity<List<GetTeamRankingResponse>> getTeamRanking() {
        return ResponseEntity.ok(getTeamRankingService.execute());
    }

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
}