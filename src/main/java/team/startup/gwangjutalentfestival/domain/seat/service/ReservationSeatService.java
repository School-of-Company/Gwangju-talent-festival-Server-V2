package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BulkReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;

import java.util.List;

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

    /**
     * 참가자가 요청한 좌석들을 하나의 트랜잭션으로 예약한다.
     *
     * @param request 예약할 1~2개의 좌석
     * @return 최종 예약된 요청 좌석 목록
     */
    List<GetSeatResponse> executeBulk(BulkReservationSeatRequest request);
}
