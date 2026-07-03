package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

/**
 * 심사 조회 응답 DTO.
 *
 * @param judgementId                   심사 엔티티 ID (미심사 시 {@code null})
 * @param teamId                        팀 ID
 * @param teamName                      팀명
 * @param expressionCommunicationScore  표현·소통 점수
 * @param technicalCompletenessScore    기술·완성도 점수
 * @param creativityCompositionScore    창의·구성 점수
 * @param stagePresencePerformanceScore 무대 장악력·퍼포먼스 점수
 * @param teamworkStageHarmonyScore     팀워크·무대 조화 점수
 * @param totalScore                    팀의 전체 심사위원 합산 총점
 * @param isPerformed                   공연 완료 여부
 * @param isJudged                      현재 심사위원의 심사 완료 여부
 */
public record GetJudgementResponse(
        Long judgementId,
        Long teamId,
        String teamName,
        Integer expressionCommunicationScore,
        Integer technicalCompletenessScore,
        Integer creativityCompositionScore,
        Integer stagePresencePerformanceScore,
        Integer teamworkStageHarmonyScore,
        int totalScore,
        Boolean isPerformed,
        Boolean isJudged
) {
}
