package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.global.util.OperationMetricRecorder;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class ReservationSeatServiceImpl implements ReservationSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatReservationValidator seatReservationValidator;
    private final SeatUtil seatUtil;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OperationMetricRecorder metricRecorder;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute(ReservationSeatRequest request) {
        metricRecorder.record(
                "seat.reservation.duration",
                "seat.reservation.success",
                "seat.reservation.failure",
                () -> {
                    String seatSection = request.seatSection();
                    Integer seatNumber = request.seatNumber();

                    seatReservationValidator.validateSeatRange(seatNumber, seatUtil.getMaxSeats(seatSection));
                    seatReservationValidator.validateSeatAvailability(seatSection, seatNumber);
                    seatReservationValidator.validateReservationLimit();

                    SeatEntity seat = SeatEntity.builder()
                            .seatNumber(seatNumber)
                            .seatSection(seatSection)
                            .user(userUtil.getCurrentUserRef())
                            .build();

                    try {
                        seatReservationRepository.saveAndFlush(seat);
                    } catch (DataIntegrityViolationException e) {
                        throw new SeatAlreadyReservedException();
                    }

                    applicationEventPublisher.publishEvent(new SeatChangeEvent(
                            request.seatSection(),
                            request.seatNumber(),
                            false
                    ));
                }
        );
    }
}
