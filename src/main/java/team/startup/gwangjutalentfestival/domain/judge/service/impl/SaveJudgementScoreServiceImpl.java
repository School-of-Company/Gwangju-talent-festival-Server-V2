package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgementScoreRequest;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgementScoreService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.OperationMetricRecorder;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.Collections;
import java.util.List;

/**
 * {@link SaveJudgementScoreService} 구현체.
 * 심사 점수를 저장 또는 수정하고, 팀 총점을 갱신한다.
 * 점수 저장 후 팀 랭킹 캐시를 초기화한다.
 */
@Service
@RequiredArgsConstructor
public class SaveJudgementScoreServiceImpl implements SaveJudgementScoreService {

    private final JudgementRepository judgementRepository;
    private final TeamRepository teamRepository;
    private final UserUtil userUtil;
    private final OperationMetricRecorder metricRecorder;

    /**
     * 현재 로그인한 심사위원의 특정 팀 심사 점수를 저장하거나 수정한다.
     * 기존 심사 데이터가 있으면 점수를 갱신하고, 없으면 새로 생성한다.
     * 저장 후 팀 총점(전체 심사위원 합산 점수)을 재계산한다.
     *
     * @param request 심사 점수 요청 데이터
     * @param teamId  대상 팀 ID
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.TEAM_RANKING, allEntries = true)
    public void execute(SaveJudgementScoreRequest request, Long teamId) {
        metricRecorder.record(
                "judge.submit.duration",
                "judge.submit.success",
                "judge.submit.failure",
                () -> {
                    UserEntity user = userUtil.getCurrentUser();
                    TeamEntity team = teamRepository.findByIdForUpdate(teamId)
                            .orElseThrow(TeamNotFoundException::new);

                    judgementRepository.findByTeamAndUser(team, user)
                            .ifPresentOrElse(
                                    judgement -> judgement.updateScore(
                                            request.completenessExpressionScore(),
                                            request.creativityCompositionScore(),
                                            request.stagePerformanceTeamworkScore()
                                    ),
                                    () -> judgementRepository.save(JudgementEntity.builder()
                                            .completenessExpressionScore(request.completenessExpressionScore())
                                            .creativityCompositionScore(request.creativityCompositionScore())
                                            .stagePerformanceTeamworkScore(request.stagePerformanceTeamworkScore())
                                            .team(team)
                                            .user(user)
                                            .build()));
                    updateTotalScore(team);
                }
        );
    }

    /**
     * 팀 총점을 재계산한다.
     * 심사위원 수와 무관하게 최고점과 최저점을 제외한 나머지 점수의 평균(반올림)으로 계산한다.
     * 예선/본선 등 라운드마다 채점 인원이 달라도 동일한 기준이 적용된다.
     *
     * @param team 총점을 갱신할 팀 엔티티
     */
    private void updateTotalScore(TeamEntity team) {
        List<Integer> scores = judgementRepository.findAllJudgeTotalScoresByTeam(team);
        team.updateTotalScore(calculateTotalScore(scores));
    }

    private int calculateTotalScore(List<Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        int sum = scores.stream().mapToInt(Integer::intValue).sum();
        // ponytail: 최고/최저를 제외하면 최소 1명은 남아야 트리밍이 의미 있으므로 3명 미만은 단순 평균
        if (scores.size() < 3) {
            return Math.round(sum / (float) scores.size());
        }
        int max = Collections.max(scores);
        int min = Collections.min(scores);
        int remainingCount = scores.size() - 2;
        return Math.round((sum - max - min) / (float) remainingCount);
    }
}