package team.startup.gwangjutalentfestival.domain.seat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

@Entity
@Table(
        name = "seat_ban",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"seat_section", "seat_number"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatBanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_section", nullable = false)
    private Character seatSection;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
}
