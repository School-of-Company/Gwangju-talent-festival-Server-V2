package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBanNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;

/**
 * {@link CancelSeatBanService}의 구현체.
 * 좌석 차단 해제 후 SSE 이벤트를 발행하여 클라이언트에 변경 사항을 알린다.
 */
@Service
@RequiredArgsConstructor
public class CancelSeatBanServiceImpl implements CancelSeatBanService {

    private final SeatBanRepository seatBanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 요청한 좌석의 차단을 해제하고 SSE 이벤트를 발행한다.
     *
     * @param request 차단 해제할 좌석의 구역 및 번호 정보
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.SeatBanNotFoundException 해당 좌석의 차단 정보가 없을 때
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SEATS_ALL, allEntries = true),
            @CacheEvict(value = CacheConfig.SEATS_SECTION, allEntries = true)
    })
    public void execute(CancelSeatBanRequest request) {
        SeatBanEntity seatBan = seatBanRepository
                .findBySeatSectionAndSeatNumber(request.seatSection(), request.seatNumber())
                .orElseThrow(SeatBanNotFoundException::new);

        seatBanRepository.delete(seatBan);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                true
        ));
    }
}
