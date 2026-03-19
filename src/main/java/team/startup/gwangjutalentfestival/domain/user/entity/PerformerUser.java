package team.startup.gwangjutalentfestival.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "performs_user")
public class PerformerUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false,name = "password")
    private String password;
}
