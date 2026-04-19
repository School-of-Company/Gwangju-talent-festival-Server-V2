package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgementTeamEvent;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamPerformanceService;

/**
 * {@link UpdateTeamPerformanceService} 구현체.
 * 팀 상태 변경 후 전체 팀 및 랭킹 캐시를 초기화하고 심사 이벤트를 발행한다.
 */
@Service
@RequiredArgsConstructor
public class UpdateTeamPerformanceServiceImpl implements UpdateTeamPerformanceService {
    private final TeamRepository teamRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 특정 팀의 공연 상태를 다음 단계로 변경한다.
     * 팀이 존재하지 않으면 {@link team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException}이 발생한다.
     * 이미 완료된 팀이면 {@link team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyFinishedException}이 발생한다.
     * 상태 변경 후 전체 팀 및 랭킹 캐시를 초기화하고 심사 이벤트를 발행한다.
     *
     * @param teamId 상태를 변경할 팀 ID
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.TEAM_ALL,     allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TEAM_RANKING, allEntries = true)
    })
    public void execute(Long teamId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.updateStatus(team.getTeamStatus().next());

        applicationEventPublisher.publishEvent(new JudgementTeamEvent(teamId));
    }
}
