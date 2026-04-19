package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
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
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import static team.startup.gwangjutalentfestival.domain.user.enums.Role.PERFORMER;

/**
 * {@link ReservationSeatService}의 구현체.
 * 좌석 유효성, 예약·차단 여부, 예약 한도를 검증한 후 좌석을 예약하고 SSE 이벤트를 발행한다.
 * 동시 요청 충돌은 DB 유니크 제약을 통해 처리하며, 관련 캐시를 무효화한다.
 */
@Service
@RequiredArgsConstructor
public class ReservationSeatServiceImpl implements ReservationSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatUtil seatUtil;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int PERFORMER_SEAT_LIMIT = 3;
    private static final int DEFAULT_SEAT_LIMIT = 1;
    private static final int RESERVED = 1;
    private static final int BANNED = 2;

    /**
     * 요청한 좌석을 현재 로그인한 사용자에게 예약한다.
     *
     * @param request 예약할 좌석의 구역 및 번호 정보
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException 좌석 번호가 구역 범위를 벗어날 때
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException 이미 예약된 좌석일 때
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException 차단된 좌석일 때
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException 예약 한도를 초과했을 때
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute(ReservationSeatRequest request) {
        String seatSection = request.seatSection();
        Integer seatNumber = request.seatNumber();

        Integer maxSeats = seatUtil.getMaxSeats(seatSection);
        if (seatNumber < 1 || seatNumber > maxSeats) {
            throw new SeatNotExistsInSectionException();
        }

        int availability = seatReservationRepository.checkAvailability(seatSection, seatNumber);
        if ((availability & RESERVED) != 0) throw new SeatAlreadyReservedException();
        if ((availability & BANNED) != 0) throw new SeatBannedException();

        long userId = UserUtil.getCurrentUserId();
        long reserveCount = seatReservationRepository.countByUserId(userId);

        int limit = UserUtil.getCurrentUserRole() == PERFORMER ? PERFORMER_SEAT_LIMIT : DEFAULT_SEAT_LIMIT;

        if (reserveCount >= limit) {
            throw new SeatReservationLimitExceededException();
        }

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
}
