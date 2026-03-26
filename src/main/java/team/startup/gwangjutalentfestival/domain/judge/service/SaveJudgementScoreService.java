package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgementScoreRequest;

public interface SaveJudgementScoreService {
    void execute(SaveJudgementScoreRequest request, Long teamId);
}
