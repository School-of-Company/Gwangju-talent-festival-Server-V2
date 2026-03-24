package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.GetAllTeamService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllTeamServiceImpl implements GetAllTeamService {
    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GetTeamResponse> execute() {
        return teamRepository.findAllByOrderByPerformOrderAsc()
                .stream()
                .map(t -> new GetTeamResponse(
                        t.getId(),
                        t.getTeamName(),
                        t.getSchool(),
                        t.getPerformOrder(),
                        t.getTeamStatus()
                )).toList();
    }
}
