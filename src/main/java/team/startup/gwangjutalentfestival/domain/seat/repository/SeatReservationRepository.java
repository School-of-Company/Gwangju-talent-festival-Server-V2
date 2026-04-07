package team.startup.gwangjutalentfestival.domain.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface SeatReservationRepository extends JpaRepository<SeatEntity, Long> {
    boolean existsBySeatSectionAndSeatNumber(String seatSection, Integer seatNumber);

    @Query(value =
            "SELECT EXISTS(SELECT 1 FROM seat WHERE seat_section = :section AND seat_number = :number)" +
            " + EXISTS(SELECT 1 FROM seat_ban WHERE seat_section = :section AND seat_number = :number) * 2",
            nativeQuery = true)
    int checkAvailability(@Param("section") String seatSection, @Param("number") Integer seatNumber);

    long countByUser(UserEntity user);

    long countByUserId(Long userId);

    Optional<SeatEntity> findByUser(UserEntity user);

    Optional<SeatEntity> findBySeatSectionAndSeatNumberAndUser(String seatSection, Integer seatNumber, UserEntity user);

    List<SeatEntity> findAllByUserId(Long userId);
}
