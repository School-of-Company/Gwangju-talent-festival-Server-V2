package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.GetAllTeamService;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;

import java.util.List;

/**
 * {@link GetAllTeamService} 구현체.
 * 결과가 비어있지 않은 경우 캐싱하여 반복 조회 성능을 최적화한다.
 */
@Service
@RequiredArgsConstructor
public class GetAllTeamServiceImpl implements GetAllTeamService {
    private final TeamRepository teamRepository;

    /**
     * 공연 순서 오름차순으로 전체 팀 목록을 조회한다.
     * 결과가 비어있지 않으면 캐시에 저장된다.
     *
     * @return 전체 팀 응답 목록
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.TEAM_ALL, unless = "#result.isEmpty()")
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
