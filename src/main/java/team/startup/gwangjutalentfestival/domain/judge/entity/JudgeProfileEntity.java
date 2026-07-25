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
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "judge_profile", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class JudgeProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = JsonNodeAttributeConverter.class)
    @Column(name = "affiliation_strokes", columnDefinition = "JSON", nullable = false)
    private JsonNode affiliationStrokes;

    @Convert(converter = JsonNodeAttributeConverter.class)
    @Column(name = "position_strokes", columnDefinition = "JSON", nullable = false)
    private JsonNode positionStrokes;

    @Convert(converter = JsonNodeAttributeConverter.class)
    @Column(name = "name_strokes", columnDefinition = "JSON", nullable = false)
    private JsonNode nameStrokes;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
