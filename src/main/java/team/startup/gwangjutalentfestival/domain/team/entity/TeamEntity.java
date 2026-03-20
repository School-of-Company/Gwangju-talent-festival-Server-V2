package team.startup.gwangjutalentfestival.domain.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;

import java.time.LocalDate;

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

    @Column(name = "team_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamStatus teamStatus;

    @Column(name = "event_year", nullable = false)
    private LocalDate eventYear;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;
}
