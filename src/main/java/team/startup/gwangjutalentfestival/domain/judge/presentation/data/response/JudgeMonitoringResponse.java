package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record JudgeMonitoringResponse(
        long version,
        List<JudgeHeader> judges,
        List<ScoreRow> scoreRows,
        List<CommentRow> commentRows
) {
    public record JudgeHeader(Long judgeId, String label) {
    }

    public record ScoreRow(
            Long teamId,
            Integer performOrder,
            String teamName,
            List<ScoreCell> scores,
            int calculatedScore,
            int rank
    ) {
    }

    public record ScoreCell(Long judgeId, Integer score) {
    }

    public record CommentRow(
            Long teamId,
            Integer performOrder,
            String teamName,
            List<CommentCell> comments
    ) {
    }

    public record CommentCell(Long judgeId, JsonNode strokes) {
    }
}
