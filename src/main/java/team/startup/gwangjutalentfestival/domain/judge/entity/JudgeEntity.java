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

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "judgement",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"})
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
}
