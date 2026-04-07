package team.startup.gwangjutalentfestival.domain.seat.repository.custom;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;

import java.util.List;
import java.util.Set;

public interface SeatReservationCustomRepository {
    List<SectionSeatNumber> findAllSeatNumbers();
    Set<Integer> findSeatNumbersBySeatSection(String seatSection);
}
