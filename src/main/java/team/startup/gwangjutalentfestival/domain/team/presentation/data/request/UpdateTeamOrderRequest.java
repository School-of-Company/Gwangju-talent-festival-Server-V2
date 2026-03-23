package team.startup.gwangjutalentfestival.domain.team.presentation.data.request;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;

import java.util.List;

public record UpdateTeamOrderRequest(
    List<TeamOrderItem> orderItems
) {
}
