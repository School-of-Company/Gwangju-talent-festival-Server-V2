package team.startup.gwangjutalentfestival.domain.seat.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 공연자의 좌석 예약 취소 요청 DTO.
 *
 * @param seatSection 취소할 좌석 구역 (A, B, C, D, E, F, W 중 하나)
 * @param seatNumber  취소할 좌석 번호 (1~132)
 */
public record PerformerCancelSeatReservationRequest(
        @Pattern(
                regexp = "^[ABCDEFW]$",
                message = "좌석 섹션은 A, B, C, D, E, F, W 중 하나여야 합니다."
        )
        @Size(max = 1)
        String seatSection,

        @Min(value = 1, message = "좌석 번호는 최소 1번부터 존재합니다.")
        @Max(value = 132, message = "좌석 번호는 최대 132번까지 존재합니다.")
        Integer seatNumber
) {
}
