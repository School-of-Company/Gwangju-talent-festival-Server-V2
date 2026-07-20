package team.startup.gwangjutalentfestival.domain.judge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.Optional;
import java.util.List;

public interface JudgeCommentRepository extends JpaRepository<JudgeCommentEntity, Long> {
    Optional<JudgeCommentEntity> findByTeamAndUser(TeamEntity team, UserEntity user);

    /**
     * (team_id, user_id) 유니크 제약을 이용한 원자적 upsert.
     * 동시에 같은 팀/심사위원 조합으로 첫 저장이 들어와도 유니크 제약 위반 없이 하나만 반영된다.
     */
    @Modifying
    @Query(value = """
            INSERT INTO judge_comment (team_id, user_id, strokes)
            VALUES (:teamId, :userId, :strokes)
            ON DUPLICATE KEY UPDATE strokes = :strokes
            """, nativeQuery = true)
    void upsert(@Param("teamId") Long teamId, @Param("userId") Long userId, @Param("strokes") String strokes);

    @Query("SELECT c FROM JudgeCommentEntity c JOIN FETCH c.user JOIN FETCH c.team")
    List<JudgeCommentEntity> findAllWithUserAndTeam();
}
