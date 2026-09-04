package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.GetSeatsByPhoneNumberService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

import java.util.List;

/**
 * {@link GetSeatsByPhoneNumberService}의 구현체.
 * 전화번호로 사용자를 찾은 뒤 해당 사용자가 예매한 좌석 전체를 조회한다.
 */
@Service
@RequiredArgsConstructor
public class GetSeatsByPhoneNumberServiceImpl implements GetSeatsByPhoneNumberService {

    private final UserRepository userRepository;
    private final SeatReservationRepository seatReservationRepository;

    /**
     * 전화번호에 해당하는 사용자의 예매 좌석 목록을 반환한다.
     *
     * @param phoneNumber 조회할 사용자의 전화번호
     * @return 예매 좌석 목록 (예매한 좌석이 없으면 빈 목록)
     * @throws UserNotFoundException 해당 전화번호의 사용자가 없을 때
     */
    @Override
    @Transactional(readOnly = true)
    public List<GetSeatResponse> execute(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(UserNotFoundException::new);

        return seatReservationRepository.findAllByUserId(user.getId()).stream()
                .map(seat -> new GetSeatResponse(
                        seat.getSeatSection(),
                        seat.getSeatNumber()
                ))
                .toList();
    }
}
