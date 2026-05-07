package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 차단 취소 요청 시 해당 구역·번호의 차단 정보가 존재하지 않을 때 발생하는 예외.
 */
public class SeatBanNotFoundException extends GlobalException {
    public SeatBanNotFoundException() {
        super(ErrorCode.SEAT_BAN_NOT_FOUND);
    }
}
