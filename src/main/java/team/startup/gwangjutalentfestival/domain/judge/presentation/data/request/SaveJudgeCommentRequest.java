package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.constraints.NotNull;

public record SaveJudgeCommentRequest(
        @NotNull ArrayNode strokes
) {
}