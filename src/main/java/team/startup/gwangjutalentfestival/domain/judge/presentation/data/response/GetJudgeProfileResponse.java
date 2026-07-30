package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

import com.fasterxml.jackson.databind.JsonNode;

public record GetJudgeProfileResponse(
        JsonNode affiliationStrokes,
        JsonNode positionStrokes,
        JsonNode nameStrokes
) {
}
