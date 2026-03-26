package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        }
        team.updateTotalScore(newTotal);
    }
}
