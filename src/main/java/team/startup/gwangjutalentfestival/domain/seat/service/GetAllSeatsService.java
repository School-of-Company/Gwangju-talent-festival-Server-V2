package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetAllSeatsResponse;

/**
 * 전체 구역의 좌석 현황을 조회하는 서비스 인터페이스.
 */
public interface GetAllSeatsService {

    /**
     * 현재 사용자 역할에 맞는 전체 구역 좌석 현황을 반환한다.
     *
     * @return 구역별 좌석 가용 여부 맵
     */
    GetAllSeatsResponse execute();
}
