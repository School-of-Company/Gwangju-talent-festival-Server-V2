package team.startup.gwangjutalentfestival.domain.team.repository;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;

import java.util.List;

public interface TeamRepositoryCustom {
    List<GetTeamRankingResponse> getRanking();
}
