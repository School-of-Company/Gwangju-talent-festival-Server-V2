package team.startup.gwangjutalentfestival.domain.team.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;

import java.util.List;

/**
 * 팀 공연 순서가 변경되었을 때 발행되는 이벤트.
 * 변경된 팀 순서 목록을 담아 이벤트 리스너에 전달한다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamOrderChangedEvent {
    List<TeamOrderItem> orders;
}
