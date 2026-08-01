package team.startup.gwangjutalentfestival.domain.seat.repository.custom;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import java.util.List;
import java.util.Set;

/**
 * 좌석 차단 정보에 대한 QueryDSL 커스텀 레포지토리 인터페이스.
 */
public interface SeatBanCustomRepository {

    /**
     * 모든 차단 좌석의 구역과 번호 목록을 조회한다.
     *
     * @return 차단된 좌석의 구역·번호 목록
     */
    List<SectionSeatNumber> findAllSeatNumbers();

    /**
     * 특정 구역의 차단 좌석 번호 집합을 조회한다.
     *
     * @param seatSection 좌석 구역
     * @return 차단된 좌석 번호 집합
     */
    Set<Integer> findSeatNumbersBySeatSection(String seatSection);
}
