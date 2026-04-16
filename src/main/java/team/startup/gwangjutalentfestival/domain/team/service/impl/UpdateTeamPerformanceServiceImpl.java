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

@Service
@RequiredArgsConstructor
public class UpdateTeamPerformanceServiceImpl implements UpdateTeamPerformanceService {
    private final TeamRepository teamRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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
