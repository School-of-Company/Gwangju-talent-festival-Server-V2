package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;

import java.util.List;

public interface GetAllJudgementService {
    List<GetJudgementResponse> execute();
}
