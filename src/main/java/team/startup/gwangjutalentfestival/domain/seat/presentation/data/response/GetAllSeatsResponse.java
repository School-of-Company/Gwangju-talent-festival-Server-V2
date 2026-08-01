package team.startup.gwangjutalentfestival.domain.seat.presentation.data.response;

import java.util.Map;

/**
 * 전체 구역 좌석 현황 응답 DTO.
 *
 * @param sections 구역 코드(A~F)를 키로, 해당 구역의 좌석 목록을 값으로 갖는 맵
 */
public record GetAllSeatsResponse(
    Map<String, GetSeatsBySectionResponse> sections
) {
}
