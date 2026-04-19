package team.startup.gwangjutalentfestival.domain.seat.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 좌석 차단 취소 요청 DTO.
 *
 * @param seatSection 차단을 해제할 좌석 구역 (A~J)
 * @param seatNumber  차단을 해제할 좌석 번호 (1~154)
 */
public record CancelSeatBanRequest(
        @Pattern(
                regexp = "^[A-J]$",
                message = "좌석 섹션은 A부터 J까지 존재합니다."
        )
        @Size(max = 1)
        String seatSection,

        @Min(value = 1, message = "좌석 번호는 최소 1번부터 존재합니다.")
        @Max(value = 154, message = "좌석 번호는 최대 154번까지 존재합니다.")
        Integer seatNumber
) {
}
