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
import java.util.stream.IntStream;

/**
 * {@link GetAllSeatsService}의 구현체.
 * 현재 사용자 역할을 기준으로 전체 구역 좌석 현황을 조회하며, Redis 캐시를 활용한다.
 */
@Service
@RequiredArgsConstructor
public class GetAllSeatsServiceImpl implements GetAllSeatsService {

    private final UserUtil userUtil;
    private final SeatUtil seatUtil;
    private final SeatBanCustomRepository seatBanCustomRepository;
    private final SeatReservationCustomRepository seatReservationCustomRepository;

    /**
     * 현재 사용자 역할에 맞는 전체 구역 좌석 현황을 반환한다.
     * 역할별로 캐시가 분리되어 적용된다.
     *
     * @return 구역별 좌석 가용 여부 맵
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.SEATS_ALL, key = "@userUtil.currentUserRole()")
    public GetAllSeatsResponse execute() {
        Role role = userUtil.currentUserRole();

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
            responseMap.put(section, getSeatResponse(section, bans, reservations));
        }

        return new GetAllSeatsResponse(responseMap);
    }

    /**
     * 특정 구역의 차단·예약 정보를 기반으로 좌석 가용 여부 목록을 생성한다.
     *
     * @param section      좌석 구역
     * @param bans         차단된 좌석 번호 집합
     * @param reservations 예약된 좌석 번호 집합
     * @return 좌석별 예약 가능 여부 목록
     */
    private GetSeatsBySectionResponse getSeatResponse(
            String section, Set<Integer> bans, Set<Integer> reservations) {
        Integer seatLastNumber = seatUtil.getMaxSeats(section);

        List<Boolean> seats = IntStream.rangeClosed(1, seatLastNumber)
                .mapToObj(i -> !bans.contains(i) && !reservations.contains(i))
                .toList();

        return new GetSeatsBySectionResponse(seats);
    }
}
