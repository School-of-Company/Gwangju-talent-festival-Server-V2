package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
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

@Service
@RequiredArgsConstructor
public class GetAllSeatsServiceImpl implements GetAllSeatsService {

    private final SeatUtil seatUtil;
    private final SeatBanCustomRepository seatBanCustomRepository;
    private final SeatReservationCustomRepository seatReservationCustomRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.SEATS_ALL, key = "T(team.startup.gwangjutalentfestival.global.util.UserUtil).getCurrentUserRole()")
    public GetAllSeatsResponse execute() {
        Role role = UserUtil.getCurrentUserRole();

        List<SectionSeatNumber> seatBans = seatBanCustomRepository.findSeatNumbersByRole(role);
        List<SectionSeatNumber> seatReservations = seatReservationCustomRepository.findAllSeatNumbers();

        Map<String, Set<Integer>> seatBansMap = seatBans.stream()
                .collect(Collectors.groupingBy(
                        SectionSeatNumber::section,
                        Collectors.mapping(SectionSeatNumber::seatNumber, Collectors.toSet())
                ));

        Map<String, Set<Integer>> seatReservationMap = seatReservations.stream()
                .collect(Collectors.groupingBy(
                        SectionSeatNumber::section,
                        Collectors.mapping(SectionSeatNumber::seatNumber, Collectors.toSet())
                ));

        Map<String, GetSeatsBySectionResponse> responseMap = new LinkedHashMap<>();
        for (String section : seatUtil.getSections()) {
            Set<Integer> bans = seatBansMap.getOrDefault(section, Set.of());
            Set<Integer> reservations = seatReservationMap.getOrDefault(section, Set.of());
            responseMap.put(section, seatUtil.buildSeatAvailability(section, bans, reservations));
        }

        return new GetAllSeatsResponse(responseMap);
    }
}
