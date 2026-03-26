package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaveJudgementScoreRequest(
        @NotNull @Min(0) @Max(40) Integer expressionCommunicationScore,
        @NotNull @Min(0) @Max(40) Integer technicalCompletenessScore,
        @NotNull @Min(0) @Max(30) Integer creativityCompositionScore,
        @NotNull @Min(0) @Max(30) Integer stagePresencePerformanceScore,
        @NotNull @Min(0) @Max(40) Integer teamworkStageHarmonyScore
) {
}
