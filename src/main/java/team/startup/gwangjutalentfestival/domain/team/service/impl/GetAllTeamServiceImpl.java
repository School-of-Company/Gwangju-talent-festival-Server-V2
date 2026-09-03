package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.GetAllTeamService;

import java.util.List;

/**
 * {@link GetAllTeamService} 구현체.
 * 공연 순서 변경이 즉시 반영되어야 하므로 캐싱하지 않는다.
 */
@Service
@RequiredArgsConstructor
public class GetAllTeamServiceImpl implements GetAllTeamService {
    private final TeamRepository teamRepository;

    /**
     * 공연 순서 오름차순으로 전체 팀 목록을 조회한다.
     *
     * @return 전체 팀 응답 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<GetTeamResponse> execute() {
        return teamRepository.findAllByOrderByPerformOrderAsc()
                .stream()
                .map(t -> new GetTeamResponse(
                        t.getId(),
                        t.getTeamName(),
                        t.getSchool(),
                        t.getTeamGenre(),
                        t.getApplicantName(),
                        t.getPerformOrder(),
                        t.getTeamStatus()
                )).toList();
    }
}
