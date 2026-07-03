package team.startup.gwangjutalentfestival.domain.team.presentation.data.response;

import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

/**
 * 팀 정보 조회 응답 DTO.
 *
 * @param teamId       팀 ID
 * @param teamName     팀 이름
 * @param school       소속 학교
 * @param performOrder 공연 순서
 * @param status       현재 공연 상태
 */
public record GetTeamResponse(
        Long teamId,
        String teamName,
        String school,
        Integer performOrder,
        TeamStatus status
) {
}
