package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.exception.JudgementTotalScoreExceededException;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgementScoreRequest;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgementScoreService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.OperationMetricRecorder;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

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
     * 저장 후 팀 총점을 재계산하며, 총점이 100점을 초과하면 예외를 발생시킨다.
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
                    TeamEntity team = teamRepository.findById(teamId)
                            .orElseThrow(TeamNotFoundException::new);

                    judgementRepository.findByTeamAndUser(team, user)
                            .ifPresentOrElse(
                                    judgement -> judgement.updateScore(
                                            request.expressionCommunicationScore(),
                                            request.technicalCompletenessScore(),
                                            request.creativityCompositionScore(),
                                            request.stagePresencePerformanceScore(),
                                            request.teamworkStageHarmonyScore()
                                    ),
                                    () -> judgementRepository.save(JudgementEntity.builder()
                                            .expressionCommunicationScore(request.expressionCommunicationScore())
                                            .technicalCompletenessScore(request.technicalCompletenessScore())
                                            .creativityCompositionScore(request.creativityCompositionScore())
                                            .stagePresencePerformanceScore(request.stagePresencePerformanceScore())
                                            .teamworkStageHarmonyScore(request.teamworkStageHarmonyScore())
                                            .team(team)
                                            .user(user)
                                            .build()));
                    updateTotalScore(team);
                }
        );
    }

    private void updateTotalScore(TeamEntity team) {
        Integer total = judgementRepository.sumTotalScoreByTeam(team);
        int newTotal = total != null ? total : 0;
        if (newTotal > 100) {
            throw new JudgementTotalScoreExceededException();
        }
        team.updateTotalScore(newTotal);
    }
}