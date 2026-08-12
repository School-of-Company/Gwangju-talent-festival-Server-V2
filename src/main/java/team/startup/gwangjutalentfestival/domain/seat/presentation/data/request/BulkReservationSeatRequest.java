package team.startup.gwangjutalentfestival.domain.seat.presentation.data.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 참가자 다중 좌석 예약 요청 DTO.
 *
 * @param seats 한 번에 예약할 1~2개의 좌석
 */
public record BulkReservationSeatRequest(
        @NotNull(message = "예약할 좌석 목록은 필수입니다.")
        @Size(min = 1, max = 2, message = "좌석은 한 번에 1개 이상 2개 이하로 예약할 수 있습니다.")
        List<@NotNull(message = "좌석 정보는 null일 수 없습니다.") @Valid ReservationSeatRequest> seats
) {
    public BulkReservationSeatRequest {
        if (seats != null) {
            seats = Collections.unmodifiableList(new ArrayList<>(seats));
        }
    }
}
