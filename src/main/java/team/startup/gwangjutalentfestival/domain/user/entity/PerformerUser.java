package team.startup.gwangjutalentfestival.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공연자 사용자 엔티티.
 * 공연팀에 속한 사용자의 전화번호와 비밀번호를 관리한다.
 */
@Getter
@Entity
@NoArgsConstructor
@Table(name = "performer_user")
public class PerformerUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, name = "password")
    private String password;
}
