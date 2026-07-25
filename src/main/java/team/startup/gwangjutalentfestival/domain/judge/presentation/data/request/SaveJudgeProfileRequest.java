package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.constraints.NotNull;

public record SaveJudgeProfileRequest(
        @NotNull ArrayNode affiliationStrokes,
        @NotNull ArrayNode positionStrokes,
        @NotNull ArrayNode nameStrokes
) {
}
