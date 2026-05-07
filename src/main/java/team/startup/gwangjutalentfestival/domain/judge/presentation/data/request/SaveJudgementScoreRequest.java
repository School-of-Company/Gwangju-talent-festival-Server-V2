package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 심사 점수 저장 요청 DTO.
 *
 * @param expressionCommunicationScore  표현·소통 점수 (0~40)
 * @param technicalCompletenessScore    기술·완성도 점수 (0~40)
 * @param creativityCompositionScore    창의·구성 점수 (0~30)
 * @param stagePresencePerformanceScore 무대 장악력·퍼포먼스 점수 (0~30)
 * @param teamworkStageHarmonyScore     팀워크·무대 조화 점수 (0~40)
 */
public record SaveJudgementScoreRequest(
        @NotNull @Min(0) @Max(40) Integer expressionCommunicationScore,
        @NotNull @Min(0) @Max(40) Integer technicalCompletenessScore,
        @NotNull @Min(0) @Max(30) Integer creativityCompositionScore,
        @NotNull @Min(0) @Max(30) Integer stagePresencePerformanceScore,
        @NotNull @Min(0) @Max(40) Integer teamworkStageHarmonyScore
) {
}
