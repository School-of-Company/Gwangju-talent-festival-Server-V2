package team.startup.gwangjutalentfestival.domain.seat.service.admin;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;

/**
 * 관리자가 특정 좌석의 차단을 해제하는 서비스 인터페이스.
 */
public interface CancelSeatBanService {

    /**
     * 요청한 좌석의 차단을 해제한다.
     *
     * @param request 차단 해제할 좌석의 구역 및 번호 정보
     */
    void execute(CancelSeatBanRequest request);
}
