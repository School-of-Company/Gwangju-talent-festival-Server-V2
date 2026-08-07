package team.startup.gwangjutalentfestival.domain.seat.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;

import java.util.Optional;

/**
 * 좌석 차단 정보에 대한 JPA 레포지토리.
 */
public interface SeatBanRepository extends JpaRepository<SeatBanEntity, Long> {

    /**
     * 특정 구역과 번호의 차단 정보가 존재하는지 확인한다.
     *
     * @param seatSection 좌석 구역
     * @param seatNumber  좌석 번호
     * @return 차단 정보 존재 여부
     */
    boolean existsBySeatSectionAndSeatNumber(String seatSection, Integer seatNumber);

    /**
     * 특정 구역과 번호에 해당하는 차단 정보를 조회한다.
     *
     * @param seatSection 좌석 구역
     * @param seatNumber  좌석 번호
     * @return 차단 엔티티 (없으면 empty)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SeatBanEntity> findBySeatSectionAndSeatNumber(String seatSection, Integer seatNumber);
}
