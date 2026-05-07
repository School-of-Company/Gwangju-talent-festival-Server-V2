package team.startup.gwangjutalentfestival.domain.seat.presentation.data.response;

/**
 * 단일 좌석 정보 응답 DTO.
 *
 * @param seatSection 좌석 구역 (A~J)
 * @param seatNumber  좌석 번호
 */
public record GetSeatResponse(
        String seatSection,
        Integer seatNumber
) {
}
