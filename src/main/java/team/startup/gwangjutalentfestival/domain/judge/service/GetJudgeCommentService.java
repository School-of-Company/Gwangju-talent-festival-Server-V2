package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeCommentResponse;

public interface GetJudgeCommentService {
    GetJudgeCommentResponse execute(Long teamId);
}