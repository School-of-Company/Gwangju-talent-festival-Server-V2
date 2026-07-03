package team.startup.gwangjutalentfestival.domain.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * 좌석 예약 정보에 대한 JPA 레포지토리.
 */
public interface SeatReservationRepository extends JpaRepository<SeatEntity, Long> {

    /**
     * 특정 구역과 번호의 예약 정보가 존재하는지 확인한다.
     *
     * @param seatSection 좌석 구역
     * @param seatNumber  좌석 번호
     * @return 예약 존재 여부
     */
    boolean existsBySeatSectionAndSeatNumber(String seatSection, Integer seatNumber);

    /**
     * 특정 사용자의 예약 좌석 수를 조회한다.
     *
     * @param user 사용자 엔티티
     * @return 예약 좌석 수
     */
    long countByUser(UserEntity user);

    /**
     * 특정 사용자 ID의 예약 좌석 수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 예약 좌석 수
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자의 예약 좌석을 조회한다.
     *
     * @param user 사용자 엔티티
     * @return 예약 좌석 엔티티 (없으면 empty)
     */
    Optional<SeatEntity> findByUser(UserEntity user);

    /**
     * 특정 구역·번호·사용자 조건에 해당하는 예약 좌석을 조회한다.
     *
     * @param seatSection 좌석 구역
     * @param seatNumber  좌석 번호
     * @param user        사용자 엔티티
     * @return 예약 좌석 엔티티 (없으면 empty)
     */
    Optional<SeatEntity> findBySeatSectionAndSeatNumberAndUser(String seatSection, Integer seatNumber, UserEntity user);

    /**
     * 특정 사용자 ID의 모든 예약 좌석을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 예약 좌석 목록
     */
    List<SeatEntity> findAllByUserId(Long userId);
}
