package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgementService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class GetJudgementServiceImpl implements GetJudgementService {
    private static final int DEFAULT_EXPRESSION_COMMUNICATION_SCORE = 40;
    private static final int DEFAULT_TECHNICAL_COMPLETENESS_SCORE = 30;
    private static final int DEFAULT_CREATIVITY_COMPOSITION_SCORE = 30;
    private static final int DEFAULT_STAGE_PRESENCE_PERFORMANCE_SCORE = 30;
    private static final int DEFAULT_TEAMWORK_STAGE_HARMONY_SCORE = 30;

    private final UserUtil userUtil;
    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;

    @Override
    @Transactional(readOnly = true)
    public GetJudgementResponse execute(Long teamId) {
        UserEntity user = userUtil.getCurrentUser();
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        JudgementEntity judgement = judgementRepository.findByTeamAndUser(team, user)
                .orElse(null);

        return new GetJudgementResponse(
                judgement != null ? judgement.getId() : null,
                team.getId(),
                team.getTeamName(),
                judgement != null ? judgement.getExpressionCommunicationScore() : DEFAULT_EXPRESSION_COMMUNICATION_SCORE,
                judgement != null ? judgement.getTechnicalCompletenessScore() : DEFAULT_TECHNICAL_COMPLETENESS_SCORE,
                judgement != null ? judgement.getCreativityCompositionScore() : DEFAULT_CREATIVITY_COMPOSITION_SCORE,
                judgement != null ? judgement.getStagePresencePerformanceScore() : DEFAULT_STAGE_PRESENCE_PERFORMANCE_SCORE,
                judgement != null ? judgement.getTeamworkStageHarmonyScore() : DEFAULT_TEAMWORK_STAGE_HARMONY_SCORE,
                team.getTotalScore() != null ? team.getTotalScore() : 0,
                team.getTeamStatus() != TeamStatus.PENDING,
                judgement != null
        );
    }
}
