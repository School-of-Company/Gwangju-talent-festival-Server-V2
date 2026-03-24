package team.startup.gwangjutalentfestival.domain.team.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamOrderChangedEvent {
    List<TeamOrderItem> orders;
}
