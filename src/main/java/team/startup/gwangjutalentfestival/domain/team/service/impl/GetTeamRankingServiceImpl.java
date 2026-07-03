package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.GetTeamRankingService;
import team.startup.gwangjutalentfestival.global.config.CacheConfig;

import java.util.List;

/**
 * {@link GetTeamRankingService} 구현체.
 * 결과가 비어있지 않은 경우 캐싱하여 반복 조회 성능을 최적화한다.
 */
@Service
@RequiredArgsConstructor
public class GetTeamRankingServiceImpl implements GetTeamRankingService {
    private final TeamRepository teamRepository;

    /**
     * 총점 기준으로 팀 랭킹을 조회한다.
     * 결과가 비어있지 않으면 캐시에 저장된다.
     *
     * @return 순위가 포함된 팀 랭킹 응답 목록
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.TEAM_RANKING, unless = "#result.isEmpty()")
    public List<GetTeamRankingResponse> execute() {
        return teamRepository.getRanking();
    }
}
