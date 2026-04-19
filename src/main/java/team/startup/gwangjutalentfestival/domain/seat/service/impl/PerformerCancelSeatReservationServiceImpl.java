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
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

/**
 * {@link PerformerCancelSeatReservationService}의 구현체.
 * 공연자 본인의 특정 좌석 예약을 취소하고 SSE 이벤트를 발행하며, 관련 캐시를 무효화한다.
 */
@Service
@RequiredArgsConstructor
public class PerformerCancelSeatReservationServiceImpl implements PerformerCancelSeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 공연자의 특정 좌석 예약을 취소하고 SSE 이벤트를 발행한다.
     *
     * @param request 취소할 좌석의 구역 및 번호 정보
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException 해당 좌석의 예약 정보가 없을 때
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute(PerformerCancelSeatReservationRequest request) {
        UserEntity user = userUtil.getCurrentUser();

        SeatEntity seat = seatReservationRepository.findBySeatSectionAndSeatNumberAndUser(
                request.seatSection(),
                request.seatNumber(),
                user
        ).orElseThrow(SeatNotFoundException::new);

        seatReservationRepository.delete(seat);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                true
        ));
    }
}
