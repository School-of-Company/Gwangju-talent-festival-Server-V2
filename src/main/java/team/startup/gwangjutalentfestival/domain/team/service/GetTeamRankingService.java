package team.startup.gwangjutalentfestival.domain.team.service;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;

import java.util.List;

public interface GetTeamRankingService {
    List<GetTeamRankingResponse> execute();
}
