package team.startup.gwangjutalentfestival.domain.seat.presentation.data.response;

/**
 * 구역과 좌석 번호를 묶어 전달하는 내부 DTO.
 * QueryDSL 프로젝션 결과를 담는 데 사용된다.
 *
 * @param section    좌석 구역 (A~F)
 * @param seatNumber 좌석 번호
 */
public record SectionSeatNumber(
        String section,
        Integer seatNumber
) {
}
