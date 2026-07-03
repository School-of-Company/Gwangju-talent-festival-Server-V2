package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;

/**
 * 공연자가 본인의 특정 좌석 예약을 취소하는 서비스 인터페이스.
 */
public interface PerformerCancelSeatReservationService {

    /**
     * 공연자의 특정 좌석 예약을 취소한다.
     *
     * @param request 취소할 좌석의 구역 및 번호 정보
     */
    void execute(PerformerCancelSeatReservationRequest request);
}
