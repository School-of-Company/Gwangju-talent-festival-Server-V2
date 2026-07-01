package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyBannedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;

/**
 * {@link BanSeatService}의 구현체.
 * 좌석 차단 후 SSE 이벤트를 발행하여 클라이언트에 변경 사항을 알린다.
 */
@Service
@RequiredArgsConstructor
public class BanSeatServiceImpl implements BanSeatService {

    private final SeatBanRepository seatBanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 요청한 좌석을 차단 처리하고 SSE 이벤트를 발행한다.
     *
     * @param request 차단할 좌석의 구역, 번호, 적용 역할 정보
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyBannedException 이미 차단된 좌석일 때
     */
    @Override
    @Transactional
    public void execute(BanSeatRequest request) {
        if (seatBanRepository
                .existsBySeatSectionAndSeatNumber(request.seatSection(), request.seatNumber())) {
            throw new SeatAlreadyBannedException();
        }

        SeatBanEntity seatBan = SeatBanEntity.builder()
                .seatSection(request.seatSection())
                .seatNumber(request.seatNumber())
                .role(request.role())
                .build();

        try {
            seatBanRepository.saveAndFlush(seatBan);
        } catch (DataIntegrityViolationException e) {
            throw new SeatAlreadyBannedException();
        }

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                false
        ));
    }
}
