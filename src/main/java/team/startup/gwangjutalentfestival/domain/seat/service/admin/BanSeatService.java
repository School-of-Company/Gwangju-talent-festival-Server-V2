package team.startup.gwangjutalentfestival.domain.seat.service.admin;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;

/**
 * 관리자가 특정 좌석을 차단하는 서비스 인터페이스.
 */
public interface BanSeatService {

    /**
     * 요청한 좌석을 특정 역할에 대해 차단 처리한다.
     *
     * @param request 차단할 좌석의 구역, 번호, 적용 역할 정보
     */
    void execute(BanSeatRequest request);
}
