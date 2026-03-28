package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;

public interface GetJudgementService {
    GetJudgementResponse execute(Long teamId);
}
