package team.startup.gwangjutalentfestival.domain.judge.presentation.data.response;

import java.util.List;

public record JudgeMonitoringDeltaResponse(
        long version,
        ScoreSnapshot scores,
        List<CommentDelta> comments
) {
    public record ScoreSnapshot(
            List<JudgeMonitoringResponse.JudgeHeader> judges,
            List<JudgeMonitoringResponse.ScoreRow> scoreRows
    ) {
    }

    public record CommentDelta(Long teamId, Long judgeId) {
    }
}
