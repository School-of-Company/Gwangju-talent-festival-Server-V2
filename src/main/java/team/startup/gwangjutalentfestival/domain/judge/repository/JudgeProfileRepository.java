package team.startup.gwangjutalentfestival.domain.judge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeProfileEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface JudgeProfileRepository extends JpaRepository<JudgeProfileEntity, Long> {

    Optional<JudgeProfileEntity> findByUser(UserEntity user);

    @Modifying
    @Query(value = """
            INSERT INTO judge_profile (user_id, affiliation_strokes, position_strokes, name_strokes)
            VALUES (:userId, :affiliationStrokes, :positionStrokes, :nameStrokes)
            ON DUPLICATE KEY UPDATE
                affiliation_strokes = :affiliationStrokes,
                position_strokes = :positionStrokes,
                name_strokes = :nameStrokes
            """, nativeQuery = true)
    void upsert(
            @Param("userId") Long userId,
            @Param("affiliationStrokes") String affiliationStrokes,
            @Param("positionStrokes") String positionStrokes,
            @Param("nameStrokes") String nameStrokes);

    @Query("SELECT p FROM JudgeProfileEntity p JOIN FETCH p.user")
    List<JudgeProfileEntity> findAllWithUser();
}
