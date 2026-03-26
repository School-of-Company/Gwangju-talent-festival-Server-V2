package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.GetByCurrentPerformerSeatsService;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetByCurrentPerformerSeatsServiceImpl implements GetByCurrentPerformerSeatsService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;

    @Override
    @Transactional(readOnly = true)
    public List<GetSeatResponse> execute() {
        List<SeatEntity> seats = seatReservationRepository.findAllByUserId(userUtil.getCurrentUser().getId());

        return seats.stream()
                .map(seat -> new GetSeatResponse(
                        seat.getSeatSection(),
                        seat.getSeatNumber()
                ))
                .toList();
    }
}
