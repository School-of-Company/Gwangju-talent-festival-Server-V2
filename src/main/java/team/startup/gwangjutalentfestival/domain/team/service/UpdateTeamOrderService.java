package team.startup.gwangjutalentfestival.domain.team.service;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;

import java.util.List;

/**
 * 팀 공연 순서 변경 서비스 인터페이스.
 */
public interface UpdateTeamOrderService {

    /**
     * 여러 팀의 공연 순서를 일괄 변경한다.
     *
     * @param orders 변경할 팀 순서 항목 목록
     */
    void execute(List<TeamOrderItem> orders);
}
