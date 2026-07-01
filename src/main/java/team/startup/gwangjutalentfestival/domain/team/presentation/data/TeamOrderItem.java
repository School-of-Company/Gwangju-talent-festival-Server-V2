package team.startup.gwangjutalentfestival.domain.team.presentation.data;

import jakarta.validation.constraints.NotNull;

/**
 * 팀 공연 순서 변경을 위한 단일 항목.
 *
 * @param teamId 순서를 변경할 팀 ID
 * @param order  변경할 공연 순서 번호
 */
public record TeamOrderItem(
        @NotNull(message = "팀 ID를 입력해주세요.")
        Long teamId,

        @NotNull(message = "공연 순서를 입력해주세요.")
        Integer order
) {
}
