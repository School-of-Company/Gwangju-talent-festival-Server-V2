package team.startup.gwangjutalentfestival.domain.seat.presentation.data.response;

import java.util.List;

/**
 * 특정 구역의 좌석 현황 응답 DTO.
 *
 * @param seats 좌석 번호 순서대로 예약 가능 여부(true: 예약 가능, false: 예약 불가)를 담은 목록
 */
public record GetSeatsBySectionResponse(
        List<Boolean> seats
) {
}
