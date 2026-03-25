package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetAllSeatsResponse;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatBanCustomRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatReservationCustomRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.GetAllSeatsService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GetAllSeatsServiceImpl implements GetAllSeatsService {

    private final UserUtil userUtil;
    private final SeatUtil seatUtil;
    private final SeatBanCustomRepository seatBanCustomRepository;
    private final SeatReservationCustomRepository seatReservationCustomRepository;

    private static final List<Character> SEAT_SECTIONS = List.of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J');

    @Override
    @Transactional(readOnly = true)
    public GetAllSeatsResponse execute() {
        Role role = userUtil.getCurrentUser().getRole();

        List<SectionSeatNumber> seatBans = seatBanCustomRepository.findSeatNumbersByRole(role);
        List<SectionSeatNumber> seatReservations = seatReservationCustomRepository.findAllSeatNumbers();

        Map<Character, Set<Integer>> seatBansMap = seatBans.stream()
                .collect(Collectors.groupingBy(
                        SectionSeatNumber::section,
                        Collectors.mapping(SectionSeatNumber::seatNumber, Collectors.toSet())
                ));

        Map<Character, Set<Integer>> seatReservationMap = seatReservations.stream()
                .collect(Collectors.groupingBy(
                        SectionSeatNumber::section,
                        Collectors.mapping(SectionSeatNumber::seatNumber, Collectors.toSet())
                ));

        Map<String, GetSeatsBySectionResponse> responseMap = new LinkedHashMap<>();

        for (Character section : SEAT_SECTIONS) {
            Set<Integer> bans = seatBansMap.getOrDefault(section, Set.of());
            Set<Integer> reservations = seatReservationMap.getOrDefault(section, Set.of());
            responseMap.put(section.toString(), getSeatResponse(section, bans, reservations));
        }

        return new GetAllSeatsResponse(responseMap);
    }

    private GetSeatsBySectionResponse getSeatResponse(
            Character section, Set<Integer> bans, Set<Integer> reservations) {
        Integer seatLastNumber = seatUtil.getMaxSeats(section);

        List<Boolean> seats = IntStream.rangeClosed(1, seatLastNumber)
                .mapToObj(i -> !bans.contains(i) && !reservations.contains(i))
                .toList();

        return new GetSeatsBySectionResponse(seats);
    }
}
