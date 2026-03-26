package team.startup.gwangjutalentfestival.domain.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;

import java.util.Optional;

public interface SeatBanRepository extends JpaRepository<SeatBanEntity, Long> {
    boolean existsBySeatSectionAndSeatNumber(String seatSection, Integer seatNumber);

    Optional<SeatBanEntity> findBySeatSectionAndSeatNumber(String seatSection, Integer seatNumber);
}
