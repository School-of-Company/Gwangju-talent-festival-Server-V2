package team.startup.gwangjutalentfestival.domain.team.service;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;

import java.util.List;

public interface UpdateTeamOrderService {
    void execute(List<TeamOrderItem> orders);
}
