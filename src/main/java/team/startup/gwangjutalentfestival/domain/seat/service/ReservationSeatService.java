package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;

/**
 * 좌석 예약을 처리하는 서비스 인터페이스.
 */
public interface ReservationSeatService {

    /**
     * 요청한 좌석을 현재 로그인한 사용자에게 예약한다.
     *
     * @param request 예약할 좌석의 구역 및 번호 정보
     */
    void execute(ReservationSeatRequest request);
}
