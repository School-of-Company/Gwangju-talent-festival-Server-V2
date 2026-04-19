package team.startup.gwangjutalentfestival.domain.team.presentation.data;

/**
 * 팀 공연 순서 변경을 위한 단일 항목.
 *
 * @param teamId 순서를 변경할 팀 ID
 * @param order  변경할 공연 순서 번호
 */
public record TeamOrderItem(
        Long teamId,
        Integer order
) {
}
