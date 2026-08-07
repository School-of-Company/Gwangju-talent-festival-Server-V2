package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatLockRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
public class CancelSeatReservationServiceImpl extends AbstractCancelSeatReservationService implements CancelSeatReservationService {

    private final UserUtil userUtil;

    public CancelSeatReservationServiceImpl(
            SeatReservationRepository seatReservationRepository,
            SeatLockRepository seatLockRepository,
            ApplicationEventPublisher applicationEventPublisher,
            UserUtil userUtil
    ) {
        super(seatReservationRepository, seatLockRepository, applicationEventPublisher);
        this.userUtil = userUtil;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute() {
        UserEntity user = userUtil.getCurrentUser();

        SeatEntity currentSeat = seatReservationRepository.findByUser(user)
                .orElseThrow(SeatNotFoundException::new);
        seatLockRepository.lock(currentSeat.getSeatSection(), currentSeat.getSeatNumber());
        SeatEntity seat = seatReservationRepository.findBySeatSectionAndSeatNumberAndUser(
                        currentSeat.getSeatSection(), currentSeat.getSeatNumber(), user)
                .orElseThrow(SeatNotFoundException::new);

        cancelAndPublish(seat);
    }
}
