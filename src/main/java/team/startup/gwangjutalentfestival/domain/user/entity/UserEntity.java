package team.startup.gwangjutalentfestival.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

/**
 * 일반 사용자 엔티티.
 * 전화번호, 비밀번호, 역할(Role)을 포함한 사용자 정보를 관리한다.
 */
@Entity
@Getter
@Builder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = true, name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;

    public void promoteToPerformer() {
        this.role = Role.PERFORMER;
    }
}
