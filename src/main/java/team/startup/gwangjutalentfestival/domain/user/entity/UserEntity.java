package team.startup.gwangjutalentfestival.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

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

    @Column(unique = true, nullable = true, name = "email")
    private String email;

    @Column(nullable = true, name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;
}
