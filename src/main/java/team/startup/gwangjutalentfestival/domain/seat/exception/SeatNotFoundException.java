package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 조회 또는 취소 요청 시 해당 사용자의 예약 좌석이 존재하지 않을 때 발생하는 예외.
 */
public class SeatNotFoundException extends GlobalException {
    public SeatNotFoundException() {
        super(ErrorCode.SEAT_NOT_FOUND);
    }
}
