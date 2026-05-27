package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationSeatServiceImpl implements ReservationSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatReservationValidator seatReservationValidator;
    private final SeatUtil seatUtil;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute(ReservationSeatRequest request) {
        long start = System.nanoTime();
        try {
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

            recordSeatMetric(start, true);
        } catch (Exception e) {
            recordSeatMetric(start, false);
            throw e;
        }
    }

    private void recordSeatMetric(long startNano, boolean success) {
        try {
            meterRegistry.timer("seat.reservation.duration")
                    .record(System.nanoTime() - startNano, TimeUnit.NANOSECONDS);
            meterRegistry.counter(success
                    ? "seat.reservation.success"
                    : "seat.reservation.failure").increment();
        } catch (Exception e) {
            log.warn("seat.reservation metric 기록 실패", e);
        }
    }
}
