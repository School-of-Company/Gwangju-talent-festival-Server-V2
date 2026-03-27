package team.startup.gwangjutalentfestival.domain.judge.mapper;

import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

@Component
public class JudgementMapper {

    private static final int DEFAULT_EXPRESSION_COMMUNICATION_SCORE = 40;
    private static final int DEFAULT_TECHNICAL_COMPLETENESS_SCORE = 30;
    private static final int DEFAULT_CREATIVITY_COMPOSITION_SCORE = 30;
    private static final int DEFAULT_STAGE_PRESENCE_PERFORMANCE_SCORE = 30;
    private static final int DEFAULT_TEAMWORK_STAGE_HARMONY_SCORE = 30;

    public static GetJudgementResponse toResponse(TeamEntity team, JudgementEntity jm) {
        return new GetJudgementResponse(
                jm != null ? jm.getId() : null,
                team.getId(),
                team.getTeamName(),
                jm != null ? jm.getExpressionCommunicationScore() : DEFAULT_EXPRESSION_COMMUNICATION_SCORE,
                jm != null ? jm.getTechnicalCompletenessScore() : DEFAULT_TECHNICAL_COMPLETENESS_SCORE,
                jm != null ? jm.getCreativityCompositionScore() : DEFAULT_CREATIVITY_COMPOSITION_SCORE,
                jm != null ? jm.getStagePresencePerformanceScore() : DEFAULT_STAGE_PRESENCE_PERFORMANCE_SCORE,
                jm != null ? jm.getTeamworkStageHarmonyScore() : DEFAULT_TEAMWORK_STAGE_HARMONY_SCORE,
                team.getTotalScore() != null ? team.getTotalScore() : 0,
                team.getTeamStatus() != TeamStatus.PENDING,
                jm != null
        );
    }
}
