package team.startup.gwangjutalentfestival.domain.slogan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "slogans")
@Builder
public class SloganEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slogan", nullable = false, columnDefinition = "TEXT")
    private String slogan;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "school", nullable = false)
    private String school;

    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "class_num", nullable = false)
    private Integer classNum;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
}
