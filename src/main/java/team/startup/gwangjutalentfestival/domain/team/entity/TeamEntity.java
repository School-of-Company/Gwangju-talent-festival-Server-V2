package team.startup.gwangjutalentfestival.domain.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "team")
public class TeamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "school", nullable = false)
    private String school;

    @Column(name = "team_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamStatus teamStatus;

    @Column(name = "team_genre", nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamGenre teamGenre;

    @Column(name = "perform_order")
    private Integer performOrder;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    public void updateStatus(TeamStatus teamStatus) {
        this.teamStatus = teamStatus;
    }

    public void updateOrder(Integer order) {
        this.performOrder = order;
    }

    public void updateTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
}
