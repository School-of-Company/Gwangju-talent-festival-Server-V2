package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

/**
 * {@link CancelSeatReservationService}의 구현체.
 * 현재 로그인한 사용자의 예약을 삭제하고 SSE 이벤트를 발행하며, 관련 캐시를 무효화한다.
 */
@Service
@RequiredArgsConstructor
public class CancelSeatReservationServiceImpl implements CancelSeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 현재 로그인한 사용자의 좌석 예약을 취소하고 SSE 이벤트를 발행한다.
     *
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException 예약된 좌석이 없을 때
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute() {
        UserEntity user = userUtil.getCurrentUser();

        SeatEntity seat = seatReservationRepository.findByUser(user)
                .orElseThrow(SeatNotFoundException::new);

        seatReservationRepository.delete(seat);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                seat.getSeatSection(),
                seat.getSeatNumber(),
                true
        ));
    }
}
