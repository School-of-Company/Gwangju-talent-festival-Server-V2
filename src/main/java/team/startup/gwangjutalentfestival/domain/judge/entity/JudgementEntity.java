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
 * 한 심사위원이 한 팀에 대해 입력한 5개 항목의 점수를 관리하며,
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

    @Column(name = "expression_communication_score", nullable = false)
    private Integer expressionCommunicationScore;

    @Column(name = "technical_completeness_score", nullable = false)
    private Integer technicalCompletenessScore;

    @Column(name = "creativity_composition_score", nullable = false)
    private Integer creativityCompositionScore;

    @Column(name = "stage_presence_performance_score", nullable = false)
    private Integer stagePresencePerformanceScore;

    @Column(name = "teamwork_stage_harmony_score", nullable = false)
    private Integer teamworkStageHarmonyScore;

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
     * @param expressionCommunicationScore  표현·소통 점수
     * @param technicalCompletenessScore    기술·완성도 점수
     * @param creativityCompositionScore    창의·구성 점수
     * @param stagePresencePerformanceScore 무대 장악력·퍼포먼스 점수
     * @param teamworkStageHarmonyScore     팀워크·무대 조화 점수
     */
    public void updateScore(
            Integer expressionCommunicationScore,
            Integer technicalCompletenessScore,
            Integer creativityCompositionScore,
            Integer stagePresencePerformanceScore,
            Integer teamworkStageHarmonyScore
    ){
        this.expressionCommunicationScore = expressionCommunicationScore;
        this.technicalCompletenessScore = technicalCompletenessScore;
        this.creativityCompositionScore = creativityCompositionScore;
        this.stagePresencePerformanceScore = stagePresencePerformanceScore;
        this.teamworkStageHarmonyScore = teamworkStageHarmonyScore;
    }
}
