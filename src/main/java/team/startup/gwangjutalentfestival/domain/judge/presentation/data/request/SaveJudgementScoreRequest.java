package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 심사 점수 저장 요청 DTO.
 *
 * @param completenessExpressionScore   완성도 및 표현력 점수 (0~40)
 * @param creativityCompositionScore    창의력과 구성 점수 (0~30)
 * @param stagePerformanceTeamworkScore 무대매너 및 퍼포먼스/팀워크·무대 조화 점수 (0~30)
 */
public record SaveJudgementScoreRequest(
        @NotNull @Min(0) @Max(40) Integer completenessExpressionScore,
        @NotNull @Min(0) @Max(30) Integer creativityCompositionScore,
        @NotNull @Min(0) @Max(30) Integer stagePerformanceTeamworkScore
) {
}
