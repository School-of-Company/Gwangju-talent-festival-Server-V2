package team.startup.gwangjutalentfestival.domain.seat.repository.custom;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.List;
import java.util.Set;

public interface SeatBanCustomRepository {
    List<SectionSeatNumber> findSeatNumbersByRole(Role role);
    Set<Integer> findSeatNumbersBySeatSectionAndRole(String seatSection, Role role);
}
