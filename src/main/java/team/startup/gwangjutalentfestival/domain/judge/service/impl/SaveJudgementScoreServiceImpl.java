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
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class SaveJudgementScoreServiceImpl implements SaveJudgementScoreService {

    private final JudgementRepository judgementRepository;
    private final TeamRepository teamRepository;
    private final UserUtil userUtil;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.TEAM_RANKING, allEntries = true)
    public void execute(SaveJudgementScoreRequest request, Long teamId) {
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

    private void updateTotalScore(TeamEntity team) {
        Integer total = judgementRepository.sumTotalScoreByTeam(team);
        int newTotal = total != null ? total : 0;
        if (newTotal > 100) {
            throw new JudgementTotalScoreExceededException();
            //TODO 총 점수가 몇점인지 나오면 예외처리 구문 변경
        }
        team.updateTotalScore(newTotal);
    }
}
