package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

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
