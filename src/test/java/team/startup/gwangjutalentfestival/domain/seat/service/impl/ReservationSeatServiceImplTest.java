package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.DuplicateSeatRequestException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BulkReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatLockRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.global.util.OperationMetricRecorder;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReservationSeatServiceImplTest {

    private ReservationSeatServiceImpl reservationSeatService;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private SeatLockRepository seatLockRepository;

    @Mock
    private SeatReservationValidator seatReservationValidator;

    @Mock
    private SeatUtil seatUtil;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CacheManager cacheManager;

    private final OperationMetricRecorder metricRecorder =
            new OperationMetricRecorder(new SimpleMeterRegistry());

    private static final String SEAT_SECTION = "A";
    private static final Integer SEAT_NUMBER = 33;
    private static final Integer MAX_SEATS = 101;
    private static final long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        reservationSeatService = new ReservationSeatServiceImpl(
                seatReservationRepository,
                seatLockRepository,
                seatReservationValidator,
                seatUtil,
                applicationEventPublisher,
                metricRecorder,
                cacheManager
        );
    }

    private ReservationSeatRequest request() {
        return new ReservationSeatRequest(SEAT_SECTION, SEAT_NUMBER);
    }

    private UserEntity userOf(Role role) {
        return UserEntity.builder()
                .id(USER_ID)
                .role(role)
                .build();
    }

    private void givenReservationContext(Role role, List<SeatEntity> existingSeats) {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationValidator.lockCurrentUser()).willReturn(userOf(role));
        given(seatReservationRepository.findAllByUserId(USER_ID)).willReturn(existingSeats);
    }

    @Test
    void USER_정상_예약_성공() {
        givenReservationContext(Role.USER, List.of());

        reservationSeatService.execute(request());

        verify(seatLockRepository).lock(SEAT_SECTION, SEAT_NUMBER);
        verify(seatReservationRepository).saveAllAndFlush(anyList());
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, false));
    }

    @Test
    void PERFORMER_두_좌석_원자적_예약_성공() {
        ReservationSeatRequest second = new ReservationSeatRequest("A", 34);
        givenReservationContext(Role.PERFORMER, List.of());

        List<GetSeatResponse> result = reservationSeatService.executeBulk(
                new BulkReservationSeatRequest(List.of(second, request())));

        assertThat(result).containsExactly(
                new GetSeatResponse("A", 34),
                new GetSeatResponse(SEAT_SECTION, SEAT_NUMBER));
        InOrder lockOrder = inOrder(seatLockRepository);
        lockOrder.verify(seatLockRepository).lock(SEAT_SECTION, SEAT_NUMBER);
        lockOrder.verify(seatLockRepository).lock("A", 34);
        verify(seatReservationRepository).saveAllAndFlush(anyList());
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, false));
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent("A", 34, false));
        verify(cacheManager).getCache(CacheConfig.SEATS_ALL);
        verify(cacheManager).getCache(CacheConfig.SEATS_SECTION);
    }

    @Test
    void 동일한_좌석이_중복되면_잠금_전에_거부한다() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        BulkReservationSeatRequest bulkRequest = new BulkReservationSeatRequest(List.of(request(), request()));

        assertThatThrownBy(() -> reservationSeatService.executeBulk(bulkRequest))
                .isInstanceOf(DuplicateSeatRequestException.class);

        verify(seatReservationValidator, never()).lockCurrentUser();
        verify(seatReservationRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void 동일한_다중_예약을_재요청하면_저장과_이벤트_없이_성공한다() {
        SeatEntity existing = SeatEntity.builder()
                .seatSection(SEAT_SECTION)
                .seatNumber(SEAT_NUMBER)
                .user(userOf(Role.PERFORMER))
                .build();
        givenReservationContext(Role.PERFORMER, List.of(existing));

        List<GetSeatResponse> result = reservationSeatService.executeBulk(
                new BulkReservationSeatRequest(List.of(request())));

        assertThat(result).containsExactly(new GetSeatResponse(SEAT_SECTION, SEAT_NUMBER));
        verify(seatReservationRepository, never()).saveAllAndFlush(anyList());
        verify(applicationEventPublisher, never()).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, false));
        verifyNoInteractions(cacheManager);
    }

    @Test
    void 신규_다중_예약의_캐시는_트랜잭션_커밋_후_제거한다() {
        ReservationSeatRequest second = new ReservationSeatRequest("A", 34);
        givenReservationContext(Role.PERFORMER, List.of());
        TransactionSynchronizationManager.initSynchronization();

        try {
            reservationSeatService.executeBulk(
                    new BulkReservationSeatRequest(List.of(request(), second)));

            verifyNoInteractions(cacheManager);
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(cacheManager).getCache(CacheConfig.SEATS_ALL);
            verify(cacheManager).getCache(CacheConfig.SEATS_SECTION);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void 기존_한_좌석과_신규_한_좌석을_요청하면_신규_좌석만_저장하고_이벤트를_발행한다() {
        SeatEntity existing = SeatEntity.builder()
                .seatSection(SEAT_SECTION)
                .seatNumber(SEAT_NUMBER)
                .user(userOf(Role.PERFORMER))
                .build();
        ReservationSeatRequest newRequest = new ReservationSeatRequest("A", 34);
        givenReservationContext(Role.PERFORMER, List.of(existing));

        reservationSeatService.executeBulk(new BulkReservationSeatRequest(List.of(request(), newRequest)));

        verify(seatReservationRepository).saveAllAndFlush(anyList());
        verify(applicationEventPublisher, never()).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, false));
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent("A", 34, false));
    }

    @Test
    void 단건_API에서_본인_좌석을_재요청하면_기존_계약대로_예외가_발생한다() {
        SeatEntity existing = SeatEntity.builder()
                .seatSection(SEAT_SECTION)
                .seatNumber(SEAT_NUMBER)
                .user(userOf(Role.USER))
                .build();
        givenReservationContext(Role.USER, List.of(existing));

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    @Test
    void 범위_밖_좌석번호_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        willThrow(SeatNotExistsInSectionException.class).given(seatReservationValidator)
                .validateSeatRange(SEAT_NUMBER, MAX_SEATS);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatNotExistsInSectionException.class);
    }

    @Test
    void 다중_요청_중_한_좌석이_이미_예약됐으면_저장하지_않는다() {
        ReservationSeatRequest second = new ReservationSeatRequest("A", 34);
        givenReservationContext(Role.PERFORMER, List.of());
        willThrow(SeatAlreadyReservedException.class).given(seatReservationValidator)
                .validateSeatAvailability("A", 34);

        assertThatThrownBy(() -> reservationSeatService.executeBulk(
                new BulkReservationSeatRequest(List.of(second, request()))))
                .isInstanceOf(SeatAlreadyReservedException.class);

        verify(seatReservationRepository, never()).saveAllAndFlush(anyList());
        verify(applicationEventPublisher, never()).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, false));
    }

    @Test
    void 밴된_좌석_예외() {
        givenReservationContext(Role.USER, List.of());
        willThrow(SeatBannedException.class).given(seatReservationValidator)
                .validateSeatAvailability(SEAT_SECTION, SEAT_NUMBER);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatBannedException.class);
    }

    @Test
    void 역할에_허용되지_않은_좌석_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        UserEntity performer = userOf(Role.PERFORMER);
        given(seatReservationValidator.lockCurrentUser()).willReturn(performer);
        willThrow(SeatBannedException.class).given(seatReservationValidator)
                .validateSeatAccess(Role.PERFORMER, SEAT_SECTION, SEAT_NUMBER);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatBannedException.class);
    }

    @Test
    void 유저_없음_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        willThrow(UserNotFoundException.class).given(seatReservationValidator).lockCurrentUser();

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 예약_한도_초과_예외() {
        givenReservationContext(Role.PERFORMER, List.of());
        willThrow(SeatReservationLimitExceededException.class).given(seatReservationValidator)
                .validateReservationLimit(Role.PERFORMER, 0, 1);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatReservationLimitExceededException.class);
    }

    @Test
    void 동시_예약으로_DataIntegrityViolation_발생시_SeatAlreadyReservedException() {
        givenReservationContext(Role.USER, List.of());
        given(seatReservationRepository.saveAllAndFlush(anyList()))
                .willThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }
}
