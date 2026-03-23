package team.startup.gwangjutalentfestival.domain.team.service;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;

import java.util.List;

public interface GetAllTeamService {
    List<GetTeamResponse> execute();
}
