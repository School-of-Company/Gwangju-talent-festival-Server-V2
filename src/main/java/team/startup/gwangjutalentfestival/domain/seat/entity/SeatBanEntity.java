package team.startup.gwangjutalentfestival.domain.seat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자가 차단한 좌석 정보를 저장하는 엔티티.
 * 동일한 구역과 번호의 조합은 유니크 제약으로 중복 차단을 방지한다.
 */
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

    @Column(name = "seat_section", nullable = false, length = 1)
    private String seatSection;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
}
