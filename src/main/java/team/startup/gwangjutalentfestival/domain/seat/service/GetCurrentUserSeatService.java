package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;

/**
 * 현재 로그인한 사용자의 예약 좌석을 조회하는 서비스 인터페이스.
 */
public interface GetCurrentUserSeatService {

    /**
     * 현재 로그인한 사용자의 예약 좌석 정보를 반환한다.
     *
     * @return 예약 좌석 구역 및 번호
     */
    GetSeatResponse execute();
}
