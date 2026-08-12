package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;

/**
 * 특정 구역의 좌석 현황을 조회하는 서비스 인터페이스.
 */
public interface GetSeatsBySectionService {

    /**
     * 현재 사용자 역할에 맞는 특정 구역의 좌석 가용 여부 목록을 반환한다.
     *
     * @param section 조회할 좌석 구역 (A~F)
     * @return 해당 구역의 좌석별 예약 가능 여부 목록
     */
    GetSeatsBySectionResponse execute(String section);
}
