package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyBannedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BanSeatServiceImplTest {

    private static final String SEAT_SECTION = "A";
    private static final Integer SEAT_NUMBER = 1;

    @Mock
    private SeatBanRepository seatBanRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private BanSeatServiceImpl service() {
        return new BanSeatServiceImpl(seatBanRepository, applicationEventPublisher);
    }

    private BanSeatRequest request() {
        return new BanSeatRequest(SEAT_SECTION, SEAT_NUMBER, Role.USER);
    }

    @Test
    void 정상_좌석_차단_성공시_이벤트가_발행된다() {
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);

        service().execute(request());

        verify(seatBanRepository).saveAndFlush(any());
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, false));
    }

    @Test
    void 이미_차단된_좌석이면_SeatAlreadyBannedException이_발생한다() {
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> service().execute(request()))
                .isInstanceOf(SeatAlreadyBannedException.class);

        verify(seatBanRepository, never()).saveAndFlush(any());
    }

    @Test
    void 동시_차단_요청으로_DataIntegrityViolation_발생시_SeatAlreadyBannedException이_발생한다() {
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.saveAndFlush(any())).willThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service().execute(request()))
                .isInstanceOf(SeatAlreadyBannedException.class);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
