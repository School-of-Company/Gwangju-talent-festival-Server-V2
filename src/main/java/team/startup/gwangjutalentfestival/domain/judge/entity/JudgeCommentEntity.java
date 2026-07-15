package team.startup.gwangjutalentfestival.domain.judge.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import team.startup.gwangjutalentfestival.domain.judge.converter.JsonNodeAttributeConverter;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

/**
 * 심사위원의 필기 코멘트(stroke 데이터)를 저장하는 엔티티.
 * strokes는 프론트엔드가 정의한 JSON 구조를 해석하지 않고 {@link JsonNodeAttributeConverter}로 그대로 저장/복원하며,
 * (team_id, user_id) 조합에 유니크 제약이 적용된다.
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "judge_comment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"})
        }
)
public class JudgeCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = JsonNodeAttributeConverter.class)
    @Column(name = "strokes", columnDefinition = "JSON", nullable = false)
    private JsonNode strokes;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}