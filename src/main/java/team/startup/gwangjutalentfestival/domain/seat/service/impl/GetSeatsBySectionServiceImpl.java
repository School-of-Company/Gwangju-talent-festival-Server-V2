package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
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

/**
 * {@link GetSeatsBySectionService}의 구현체.
 * 현재 사용자 역할을 기준으로 특정 구역의 좌석 가용 여부를 조회하며, Redis 캐시를 활용한다.
 */
@Service
@RequiredArgsConstructor
public class GetSeatsBySectionServiceImpl implements GetSeatsBySectionService {

    private final UserUtil userUtil;
    private final SeatUtil seatUtil;
    private final SeatReservationCustomRepository seatReservationCustomRepository;
    private final SeatBanCustomRepository seatBanCustomRepository;

    /**
     * 현재 사용자 역할에 맞는 특정 구역의 좌석 가용 여부 목록을 반환한다.
     * 구역·역할 조합으로 캐시가 분리되어 적용된다.
     *
     * @param section 조회할 좌석 구역 (A~J)
     * @return 해당 구역의 좌석별 예약 가능 여부 목록
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.SEATS_SECTION, key = "#section + ':' + @userUtil.currentUserRole()")
    public GetSeatsBySectionResponse execute(String section) {
        Role role = userUtil.currentUserRole();
        Integer seatLastNumber = seatUtil.getMaxSeats(section);

        Set<Integer> reservedSeatNumbers = seatReservationCustomRepository.findSeatNumbersBySeatSection(section);
        Set<Integer> bannedSeatNumbers = seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole(section, role);

        List<Boolean> seats = IntStream.rangeClosed(1, seatLastNumber)
                .mapToObj(i -> !bannedSeatNumbers.contains(i) && !reservedSeatNumbers.contains(i))
                .toList();

        return new GetSeatsBySectionResponse(seats);
    }
}
