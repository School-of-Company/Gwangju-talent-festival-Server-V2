package team.startup.gwangjutalentfestival.domain.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

/**
 * 공연 팀 엔티티.
 * 팀 이름, 학교, 공연 상태, 장르, 공연 순서, 총점 정보를 관리한다.
 */
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

    /**
     * 팀의 공연 상태를 변경한다.
     *
     * @param teamStatus 변경할 공연 상태
     */
    public void updateStatus(TeamStatus teamStatus) {
        this.teamStatus = teamStatus;
    }

    /**
     * 팀의 공연 순서를 변경한다.
     *
     * @param order 변경할 공연 순서
     */
    public void updateOrder(Integer order) {
        this.performOrder = order;
    }

    /**
     * 팀의 총점을 변경한다.
     *
     * @param totalScore 변경할 총점
     */
    public void updateTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
}
