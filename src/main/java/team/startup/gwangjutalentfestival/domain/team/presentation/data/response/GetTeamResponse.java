package team.startup.gwangjutalentfestival.domain.team.presentation.data.response;

import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

public record GetTeamResponse(
        Long teamId,
        String teamName,
        String school,
        Integer performOrder,
        TeamStatus status
) {
}
