package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgementTeamEvent;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyFinishedException;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyOngoingException;
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
    public void execute(Long teamId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        TeamStatus status = team.getTeamStatus();
        if (status == TeamStatus.FINISHED) throw new TeamAlreadyFinishedException();
        if (status == TeamStatus.ONGOING) throw new TeamAlreadyOngoingException();

        team.updateStatus(TeamStatus.ONGOING);

        applicationEventPublisher.publishEvent(new JudgementTeamEvent(teamId));
    }
}