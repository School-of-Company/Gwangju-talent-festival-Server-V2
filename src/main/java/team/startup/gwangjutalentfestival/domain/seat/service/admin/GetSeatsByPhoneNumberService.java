package team.startup.gwangjutalentfestival.domain.seat.service.admin;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;

import java.util.List;

/**
 * 전화번호로 해당 사용자가 예매한 좌석 목록을 조회하는 서비스 인터페이스.
 */
public interface GetSeatsByPhoneNumberService {

    /**
     * 전화번호에 해당하는 사용자의 예매 좌석 목록을 반환한다.
     *
     * @param phoneNumber 조회할 사용자의 전화번호
     * @return 예매 좌석 목록 (예매한 좌석이 없으면 빈 목록)
     */
    List<GetSeatResponse> execute(String phoneNumber);
}
