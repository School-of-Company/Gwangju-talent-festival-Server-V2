package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

/**
 * 심사 조회 응답 DTO.
 *
 * @param judgementId                   심사 엔티티 ID (미심사 시 {@code null})
 * @param teamId                        팀 ID
 * @param teamName                      팀명
 * @param completenessExpressionScore   완성도 및 표현력 점수
 * @param creativityCompositionScore    창의력과 구성 점수
 * @param stagePerformanceTeamworkScore 무대매너 및 퍼포먼스/팀워크·무대 조화 점수
 * @param totalScore                    팀의 전체 심사위원 합산 총점
 * @param isPerformed                   공연 완료 여부
 * @param isJudged                      현재 심사위원의 심사 완료 여부
 */
public record GetJudgementResponse(
        Long judgementId,
        Long teamId,
        String teamName,
        Integer completenessExpressionScore,
        Integer creativityCompositionScore,
        Integer stagePerformanceTeamworkScore,
        int totalScore,
        Boolean isPerformed,
        Boolean isJudged
) {
}
