package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringResponse;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringDeltaResponse;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeCommentResponse;

public interface GetJudgeMonitoringService {
    JudgeMonitoringResponse execute();

    JudgeMonitoringDeltaResponse.ScoreSnapshot executeScores();

    long nextVersion();

    GetJudgeCommentResponse getComment(Long teamId, Long judgeId);
}
