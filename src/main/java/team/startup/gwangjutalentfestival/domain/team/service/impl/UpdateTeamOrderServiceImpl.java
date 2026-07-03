package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.event.TeamOrderChangedEvent;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamOrderService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link UpdateTeamOrderService} 구현체.
 * 팀 순서 변경 후 캐시를 초기화하고 SSE 이벤트를 발행한다.
 */
@Service
@RequiredArgsConstructor
public class UpdateTeamOrderServiceImpl implements UpdateTeamOrderService {
    private final TeamRepository teamRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 여러 팀의 공연 순서를 일괄 변경한다.
     * 존재하지 않는 팀 ID가 포함된 경우 {@link team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException}이 발생한다.
     * 변경 완료 후 전체 팀 캐시를 초기화하고 {@link team.startup.gwangjutalentfestival.domain.team.event.TeamOrderChangedEvent}를 발행한다.
     *
     * @param orders 변경할 팀 순서 항목 목록
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.TEAM_ALL, allEntries = true)
    public void execute(List<TeamOrderItem> orders) {
        List<Long> teamIds = orders.stream()
                .map(TeamOrderItem::teamId)
                .toList();

        Map<Long, TeamEntity> teamMap = teamRepository.findAllByIdIn(teamIds)
                .stream()
                .collect(Collectors.toMap(TeamEntity::getId, team -> team));

        orders.forEach(order -> {
            TeamEntity team = teamMap.get(order.teamId());
            if (team == null) {
                throw new TeamNotFoundException();
            }
            team.updateOrder(order.order());
        });

        applicationEventPublisher.publishEvent(new TeamOrderChangedEvent(orders));
    }
}
