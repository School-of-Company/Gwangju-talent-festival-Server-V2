package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

import com.fasterxml.jackson.databind.JsonNode;

public record GetJudgeCommentResponse(
        Long teamId,
        JsonNode strokes
) {
}