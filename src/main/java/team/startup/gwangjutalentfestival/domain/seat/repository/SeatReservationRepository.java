package team.startup.gwangjutalentfestival.domain.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.Optional;

public interface SeatReservationRepository extends JpaRepository<SeatEntity, Long> {
    boolean existsBySeatSectionAndSeatNumber(Character seatSection, Integer seatNumber);

    long countByUser(UserEntity user);

    Optional<SeatEntity> findByUser(UserEntity user);

    Optional<SeatEntity> findBySeatSectionAndSeatNumberAndUser(Character seatSection, Integer seatNumber, UserEntity user);
}
