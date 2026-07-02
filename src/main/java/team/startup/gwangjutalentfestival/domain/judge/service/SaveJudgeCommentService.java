package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgeCommentRequest;

public interface SaveJudgeCommentService {
    void execute(SaveJudgeCommentRequest request, Long teamId);
}