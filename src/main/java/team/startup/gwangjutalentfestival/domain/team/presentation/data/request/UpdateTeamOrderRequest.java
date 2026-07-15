package team.startup.gwangjutalentfestival.domain.team.presentation.data.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;

import java.util.List;

/**
 * 팀 공연 순서 일괄 변경 요청 DTO.
 *
 * @param orderItems 변경할 팀 순서 항목 목록
 */
public record UpdateTeamOrderRequest(
    @NotEmpty(message = "변경할 팀 순서 목록을 입력해주세요.")
    @Valid
    List<TeamOrderItem> orderItems
) {
}
