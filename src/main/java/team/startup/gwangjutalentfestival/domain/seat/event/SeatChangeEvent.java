package team.startup.gwangjutalentfestival.domain.seat.event;

/**
 * 좌석 상태 변경 시 발행되는 이벤트.
 * 예약 또는 차단 처리 후 SSE로 클라이언트에 변경 사항을 전달하는 데 사용된다.
 *
 * @param seatSection  변경된 좌석의 구역 (A~F)
 * @param seatNumber   변경된 좌석 번호
 * @param isAvailable  좌석 예약 가능 여부 (true: 예약 가능, false: 예약 불가)
 */
public record SeatChangeEvent(
        String seatSection,
        Integer seatNumber,
        Boolean isAvailable
) {
}
