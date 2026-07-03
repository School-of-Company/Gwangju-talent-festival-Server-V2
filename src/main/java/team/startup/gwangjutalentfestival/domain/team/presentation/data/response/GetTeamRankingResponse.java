package team.startup.gwangjutalentfestival.domain.team.presentation.data.response;

/**
 * 팀 랭킹 조회 응답 DTO.
 *
 * @param ranking  팀의 순위
 * @param teamName 팀 이름
 */
public record GetTeamRankingResponse(
        Integer ranking,
        String teamName
) {
}
