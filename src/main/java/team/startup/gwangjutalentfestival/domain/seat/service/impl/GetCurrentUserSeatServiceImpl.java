package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.GetCurrentUserSeatService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

/**
 * {@link GetCurrentUserSeatService}의 구현체.
 * 현재 로그인한 사용자의 예약 좌석을 조회한다.
 */
@Service
@RequiredArgsConstructor
public class GetCurrentUserSeatServiceImpl implements GetCurrentUserSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;

    /**
     * 현재 로그인한 사용자의 예약 좌석 정보를 반환한다.
     *
     * @return 예약 좌석 구역 및 번호
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException 예약된 좌석이 없을 때
     */
    @Override
    @Transactional(readOnly = true)
    public GetSeatResponse execute() {
        UserEntity user = userUtil.getCurrentUser();

        SeatEntity seat = seatReservationRepository.findByUser(user)
                .orElseThrow(SeatNotFoundException::new);

        return new GetSeatResponse(
                seat.getSeatSection(),
                seat.getSeatNumber()
        );
    }
}
