package team.startup.gwangjutalentfestival.domain.seat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

/**
 * 사용자의 좌석 예약 정보를 저장하는 엔티티.
 * 동일한 구역과 번호의 조합은 유니크 제약으로 중복 예약을 방지한다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"seat_section", "seat_number"})
        },
        indexes = {
                @Index(name = "idx_seat_user_id", columnList = "user_id")
        }
)
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "seat_section", length = 1)
    private String seatSection;

    @Column(nullable = false, name = "seat_number")
    private Integer seatNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
