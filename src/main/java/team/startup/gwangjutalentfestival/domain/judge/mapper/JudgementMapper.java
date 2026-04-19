package team.startup.gwangjutalentfestival.domain.judge.mapper;

import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

/**
 * {@link JudgementEntity} 와 팀 정보를 응답 DTO로 변환하는 유틸리티 클래스.
 * 심사 데이터가 없는 경우 기본 점수로 응답을 구성한다.
 */
public final class JudgementMapper {

    private static final int DEFAULT_EXPRESSION_COMMUNICATION_SCORE = 40;
    private static final int DEFAULT_TECHNICAL_COMPLETENESS_SCORE = 30;
    private static final int DEFAULT_CREATIVITY_COMPOSITION_SCORE = 30;
    private static final int DEFAULT_STAGE_PRESENCE_PERFORMANCE_SCORE = 30;
    private static final int DEFAULT_TEAMWORK_STAGE_HARMONY_SCORE = 30;

    /**
     * 팀 엔티티와 심사 엔티티를 {@link GetJudgementResponse}로 변환한다.
     * 심사 엔티티가 {@code null}인 경우 각 항목에 기본 점수가 설정된다.
     *
     * @param team 변환 대상 팀 엔티티
     * @param jm   해당 팀에 대한 심사 엔티티 (없으면 {@code null})
     * @return 심사 조회 응답 DTO
     */
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
