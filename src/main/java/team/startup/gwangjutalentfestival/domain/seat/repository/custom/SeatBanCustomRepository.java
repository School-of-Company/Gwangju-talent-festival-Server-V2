package team.startup.gwangjutalentfestival.domain.seat.repository.custom;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.List;
import java.util.Set;

/**
 * 좌석 차단 정보에 대한 QueryDSL 커스텀 레포지토리 인터페이스.
 */
public interface SeatBanCustomRepository {

    /**
     * 특정 역할에 적용된 모든 차단 좌석의 구역과 번호 목록을 조회한다.
     *
     * @param role 조회할 사용자 역할
     * @return 차단된 좌석의 구역·번호 목록
     */
    List<SectionSeatNumber> findSeatNumbersByRole(Role role);

    /**
     * 특정 구역에서 특정 역할에 적용된 차단 좌석 번호 집합을 조회한다.
     *
     * @param seatSection 좌석 구역
     * @param role        사용자 역할
     * @return 차단된 좌석 번호 집합
     */
    Set<Integer> findSeatNumbersBySeatSectionAndRole(String seatSection, Role role);
}
