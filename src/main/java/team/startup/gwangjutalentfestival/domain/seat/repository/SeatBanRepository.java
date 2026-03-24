package team.startup.gwangjutalentfestival.domain.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;

import java.util.Optional;

public interface SeatBanRepository extends JpaRepository<SeatBanEntity, Long> {
    boolean existsBySeatSectionAndSeatNumber(Character seatSection, Integer seatNumber);

    Optional<SeatBanEntity> findBySeatSectionAndSeatNumber(Character seatSection, Integer seatNumber);
}
