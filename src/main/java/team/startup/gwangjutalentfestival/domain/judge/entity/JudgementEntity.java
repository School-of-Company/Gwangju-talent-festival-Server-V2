package team.startup.gwangjutalentfestival.domain.judge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

/**
 * 심사 점수를 저장하는 엔티티.
 * 한 심사위원이 한 팀에 대해 입력한 3개 항목(완성도및표현력 40 / 창의력과구성 30 / 무대매너및퍼포먼스 30)의 점수를 관리하며,
 * (team_id, user_id) 조합에 유니크 제약이 적용된다.
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "judgement",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_judgement_user_id", columnList = "user_id")
        }
)
public class JudgementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "completeness_expression_score", nullable = false)
    private Integer completenessExpressionScore;

    @Column(name = "creativity_composition_score", nullable = false)
    private Integer creativityCompositionScore;

    @Column(name = "stage_performance_teamwork_score", nullable = false)
    private Integer stagePerformanceTeamworkScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * 심사 점수를 새로운 값으로 갱신한다.
     *
     * @param completenessExpressionScore   완성도 및 표현력 점수
     * @param creativityCompositionScore    창의력과 구성 점수
     * @param stagePerformanceTeamworkScore 무대매너 및 퍼포먼스/팀워크·무대 조화 점수
     */
    public void updateScore(
            Integer completenessExpressionScore,
            Integer creativityCompositionScore,
            Integer stagePerformanceTeamworkScore
    ){
        this.completenessExpressionScore = completenessExpressionScore;
        this.creativityCompositionScore = creativityCompositionScore;
        this.stagePerformanceTeamworkScore = stagePerformanceTeamworkScore;
    }
}
