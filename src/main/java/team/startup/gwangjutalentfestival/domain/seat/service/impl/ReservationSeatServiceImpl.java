package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.DuplicateSeatRequestException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BulkReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatLockRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.global.util.OperationMetricRecorder;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ReservationSeatServiceImpl implements ReservationSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatReservationValidator seatReservationValidator;
    private final SeatUtil seatUtil;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OperationMetricRecorder metricRecorder;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute(ReservationSeatRequest request) {
        metricRecorder.record(
                "seat.reservation.duration",
                "seat.reservation.success",
                "seat.reservation.failure",
                () -> reserve(List.of(request), false)
        );
    }

    @Override
    @Transactional
    public List<GetSeatResponse> executeBulk(BulkReservationSeatRequest request) {
        AtomicReference<List<GetSeatResponse>> result = new AtomicReference<>();
        metricRecorder.record(
                "seat.bulk.reservation.duration",
                "seat.bulk.reservation.success",
                "seat.bulk.reservation.failure",
                () -> result.set(reserve(request.seats(), true))
        );
        return result.get();
    }

    private List<GetSeatResponse> reserve(
            List<ReservationSeatRequest> requests,
            boolean allowIdempotentReplay
    ) {
        validateRequests(requests);

        var currentUser = seatReservationValidator.lockCurrentUser();
        seatReservationValidator.validateReservationPeriod(currentUser.getRole());
        requests.forEach(request -> seatReservationValidator.validateSeatAccess(
                currentUser.getRole(), request.seatSection(), request.seatNumber()));
        requests.stream()
                .sorted(Comparator.comparing(ReservationSeatRequest::seatSection)
                        .thenComparing(ReservationSeatRequest::seatNumber))
                .forEach(request -> seatLockRepository.lock(request.seatSection(), request.seatNumber()));

        List<SeatEntity> existingReservations = seatReservationRepository.findAllByUserId(currentUser.getId());
        Set<String> ownedSeatKeys = existingReservations.stream()
                .map(seat -> seatKey(seat.getSeatSection(), seat.getSeatNumber()))
                .collect(java.util.stream.Collectors.toSet());
        List<ReservationSeatRequest> newRequests = requests.stream()
                .filter(request -> !ownedSeatKeys.contains(seatKey(request.seatSection(), request.seatNumber())))
                .toList();

        if (!allowIdempotentReplay && newRequests.size() != requests.size()) {
            throw new SeatAlreadyReservedException();
        }

        if (allowIdempotentReplay && newRequests.isEmpty()) {
            return toResponses(requests);
        }

        seatReservationValidator.validateReservationLimit(
                currentUser.getRole(), existingReservations.size(), newRequests.size());
        newRequests.forEach(request -> seatReservationValidator.validateSeatAvailability(
                request.seatSection(), request.seatNumber()));

        List<SeatEntity> newReservations = newRequests.stream()
                .map(request -> SeatEntity.builder()
                        .seatNumber(request.seatNumber())
                        .seatSection(request.seatSection())
                        .user(currentUser)
                        .build())
                .toList();

        if (!newReservations.isEmpty()) {
            try {
                seatReservationRepository.saveAllAndFlush(newReservations);
            } catch (DataIntegrityViolationException e) {
                throw new SeatAlreadyReservedException();
            }

            if (allowIdempotentReplay) {
                evictSeatCaches();
            }
            newRequests.forEach(request -> applicationEventPublisher.publishEvent(new SeatChangeEvent(
                    request.seatSection(), request.seatNumber(), false)));
        }

        return toResponses(requests);
    }

    private List<GetSeatResponse> toResponses(List<ReservationSeatRequest> requests) {
        return requests.stream()
                .map(request -> new GetSeatResponse(request.seatSection(), request.seatNumber()))
                .toList();
    }

    private void validateRequests(List<ReservationSeatRequest> requests) {
        Set<String> seatKeys = new HashSet<>();
        for (ReservationSeatRequest request : requests) {
            seatReservationValidator.validateSeatRange(
                    request.seatNumber(), seatUtil.getMaxSeats(request.seatSection()));
            if (!seatKeys.add(seatKey(request.seatSection(), request.seatNumber()))) {
                throw new DuplicateSeatRequestException();
            }
        }
    }

    private String seatKey(String seatSection, Integer seatNumber) {
        return seatSection + ":" + seatNumber;
    }

    private void evictSeatCaches() {
        clearCache(CacheConfig.SEATS_ALL);
        clearCache(CacheConfig.SEATS_SECTION);
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
