package team.startup.gwangjutalentfestival.domain.seat.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

/**
 * 좌석 차단 요청 DTO.
 *
 * @param seatSection 차단할 좌석 구역 (A, B, C, D, E, F, W 중 하나)
 * @param seatNumber  차단할 좌석 번호 (1~132)
 * @param role        차단을 적용할 사용자 역할
 */
public record BanSeatRequest(
        @Pattern(
                regexp = "^[ABCDEFW]$",
                message = "좌석 섹션은 A, B, C, D, E, F, W 중 하나여야 합니다."
        )
        @Size(max = 1)
        String seatSection,

        @Min(value = 1, message = "좌석 번호는 최소 1번부터 존재합니다.")
        @Max(value = 132, message = "좌석 번호는 최대 132번까지 존재합니다.")
        Integer seatNumber,

        Role role
) {
}
