package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
public class PerformerCancelSeatReservationServiceImpl extends AbstractCancelSeatReservationService implements PerformerCancelSeatReservationService {

    private final UserUtil userUtil;

    public PerformerCancelSeatReservationServiceImpl(
            SeatReservationRepository seatReservationRepository,
            ApplicationEventPublisher applicationEventPublisher,
            UserUtil userUtil
    ) {
        super(seatReservationRepository, applicationEventPublisher);
        this.userUtil = userUtil;
    }

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

        cancelAndPublish(seat);
    }
}
