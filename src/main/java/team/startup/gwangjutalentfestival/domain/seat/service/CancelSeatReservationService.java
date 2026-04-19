package team.startup.gwangjutalentfestival.domain.seat.service;

/**
 * 현재 로그인한 사용자의 좌석 예약을 취소하는 서비스 인터페이스.
 */
public interface CancelSeatReservationService {

    /**
     * 현재 로그인한 사용자의 좌석 예약을 취소한다.
     */
    void execute();
}
