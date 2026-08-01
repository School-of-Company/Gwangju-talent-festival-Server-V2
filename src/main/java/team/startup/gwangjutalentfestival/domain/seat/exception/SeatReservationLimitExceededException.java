package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 사용자가 예약 가능한 좌석 수 제한을 초과했을 때 발생하는 예외.
 * 일반 사용자는 1석, 공연자는 2석까지 예약 가능하다.
 */
public class SeatReservationLimitExceededException extends GlobalException {
    public SeatReservationLimitExceededException() {
        super(ErrorCode.SEAT_RESERVATION_LIMIT_EXCEEDED);
    }
}
