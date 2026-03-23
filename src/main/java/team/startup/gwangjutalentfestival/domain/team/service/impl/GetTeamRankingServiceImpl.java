package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.TeamRankingResponse;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.GetTeamRankingService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTeamRankingServiceImpl implements GetTeamRankingService {
    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GetTeamRankingResponse> execute() {

        List<TeamRankingResponse> rankings = teamRepository.getRanking();

        return rankings.stream()
                .map(dto -> new GetTeamRankingResponse(
                        dto.getRanking(),
                        dto.getTeamName()
                )).toList();
    }
}
