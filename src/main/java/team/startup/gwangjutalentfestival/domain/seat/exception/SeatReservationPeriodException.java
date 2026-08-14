package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 해당 역할의 좌석 예매 가능 기간이 아닐 때 발생하는 예외.
 */
public class SeatReservationPeriodException extends GlobalException {
    public SeatReservationPeriodException() {
        super(ErrorCode.SEAT_RESERVATION_PERIOD_INVALID);
    }
}
