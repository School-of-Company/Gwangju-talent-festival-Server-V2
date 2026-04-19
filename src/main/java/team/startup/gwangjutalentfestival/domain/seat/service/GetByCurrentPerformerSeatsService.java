package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;

import java.util.List;

/**
 * 현재 로그인한 공연자가 예약한 좌석 목록을 조회하는 서비스 인터페이스.
 */
public interface GetByCurrentPerformerSeatsService {

    /**
     * 현재 로그인한 공연자의 예약 좌석 목록을 반환한다.
     *
     * @return 공연자의 예약 좌석 목록
     */
    List<GetSeatResponse> execute();
}
