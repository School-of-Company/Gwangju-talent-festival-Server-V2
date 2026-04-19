package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 요청한 좌석 번호가 해당 구역의 좌석 범위를 벗어날 때 발생하는 예외.
 */
public class SeatNotExistsInSectionException extends GlobalException {
    public SeatNotExistsInSectionException() {
        super(ErrorCode.SEAT_NOT_EXISTS_IN_SECTION);
    }
}