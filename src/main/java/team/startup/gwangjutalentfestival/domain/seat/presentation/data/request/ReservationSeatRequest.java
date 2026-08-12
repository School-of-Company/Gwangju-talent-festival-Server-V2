package team.startup.gwangjutalentfestival.domain.seat.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;

/**
 * 좌석 예약 요청 DTO.
 *
 * @param seatSection 예약할 좌석 구역 (A, B, C, D, E, F 중 하나)
 * @param seatNumber  예약할 좌석 번호 (1~132)
 */
public record ReservationSeatRequest(
        @NotNull(message = "좌석 섹션은 필수입니다.")
        @Pattern(
                regexp = "^[ABCDEF]$",
                message = "좌석 섹션은 A, B, C, D, E, F 중 하나여야 합니다."
        )
        String seatSection,

        @NotNull(message = "좌석 번호는 필수입니다.")
        @Min(value = 1, message = "좌석 번호는 최소 1번부터 존재합니다.")
        @Max(value = SeatUtil.MAX_SEAT_NUMBER, message = "좌석 번호는 최대 132번까지 존재합니다.")
        Integer seatNumber
) {
}
