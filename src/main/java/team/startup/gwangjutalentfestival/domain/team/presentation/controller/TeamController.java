package team.startup.gwangjutalentfestival.domain.team.presentation.controller;

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

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final GetAllTeamService getAllTeamService;
    private final GetTeamRankingService getTeamRankingService;
    private final UpdateTeamPerformanceService updateTeamPerformanceService;
    private final UpdateTeamOrderService updateTeamOrderService;

    @GetMapping
    public ResponseEntity<List<GetTeamResponse>> getAllTeam() {
        return ResponseEntity.ok(getAllTeamService.execute());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<GetTeamRankingResponse>> getTeamRanking() {
        return ResponseEntity.ok(getTeamRankingService.execute());
    }

    @PatchMapping("/{teamId}/performance")
    public ResponseEntity<Void> updateTeamPerformance(@PathVariable Long teamId) {
        updateTeamPerformanceService.execute(teamId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/order")
    public ResponseEntity<Void> updateTeamOrder(@RequestBody UpdateTeamOrderRequest request) {
        updateTeamOrderService.execute(request.orderItems());
        return ResponseEntity.noContent().build();
    }
}