package team.startup.gwangjutalentfestival.domain.judge.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 심사 점수 저장 후 발행되는 이벤트.
 * SSE를 통해 팀 점수 변경 사실을 구독자에게 알리기 위해 사용된다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JudgementTeamEvent {
    /** 점수가 변경된 팀의 식별자 */
    private Long teamId;
}
