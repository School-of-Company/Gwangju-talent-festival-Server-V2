package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class CancelSeatReservationServiceImpl implements CancelSeatReservationService {

    private final UserRepository userRepository;
    private final SeatReservationRepository seatReservationRepository;

    @Override
    @Transactional
    public void execute() {
        UserEntity user = userRepository.findById(UserUtil.getCurrentUserId())
                .orElseThrow(UserNotFoundException::new);

        SeatEntity seat = seatReservationRepository.findByUser(user)
                .orElseThrow(SeatAlreadyBannedException::new);

        seatReservationRepository.delete(seat);
    }
}
