package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatBanCustomRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatReservationCustomRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.GetSeatsBySectionService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GetSeatsBySectionServiceImpl implements GetSeatsBySectionService {

    private final UserUtil userUtil;
    private final SeatUtil seatUtil;
    private final SeatReservationCustomRepository seatReservationCustomRepository;
    private final SeatBanCustomRepository seatBanCustomRepository;

    @Override
    @Transactional(readOnly = true)
    public GetSeatsBySectionResponse execute(Character section) {
        Role role = userUtil.getCurrentUser().getRole();
        Integer seatLastNumber = seatUtil.getMaxSeats(section);

        Set<Integer> reservedSeatNumbers = seatReservationCustomRepository.findSeatNumbersBySeatSection(section);
        Set<Integer> bannedSeatNumbers = seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole(section, role);

        List<Boolean> seats = IntStream.rangeClosed(1, seatLastNumber)
                .mapToObj(i -> !bannedSeatNumbers.contains(i) && !reservedSeatNumbers.contains(i))
                .toList();

        return new GetSeatsBySectionResponse(seats);
    }
}
