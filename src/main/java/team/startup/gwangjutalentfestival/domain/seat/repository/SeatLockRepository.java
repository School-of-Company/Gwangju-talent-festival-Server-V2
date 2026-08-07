package team.startup.gwangjutalentfestival.domain.seat.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SeatLockRepository {

    private final EntityManager entityManager;

    public void lock(String seatSection, Integer seatNumber) {
        String seatKey = seatSection + ":" + seatNumber;
        entityManager.createNativeQuery("SELECT seat_key FROM seat_lock WHERE seat_key = :seatKey FOR UPDATE")
                .setParameter("seatKey", seatKey)
                .getSingleResult();
    }
}
