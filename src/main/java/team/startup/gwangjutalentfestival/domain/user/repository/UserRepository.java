package team.startup.gwangjutalentfestival.domain.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.Optional;
import java.util.List;

/**
 * 사용자 엔티티에 대한 데이터 접근 레이어.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 전화번호로 사용자를 조회한다.
     *
     * @param phoneNumber 조회할 전화번호
     * @return 해당 전화번호의 사용자 (없으면 빈 Optional)
     */
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    /**
     * 전화번호로 사용자 존재 여부를 확인한다.
     *
     * @param phoneNumber 확인할 전화번호
     * @return 사용자가 존재하면 {@code true}
     */
    boolean existsByPhoneNumber(String phoneNumber);

    List<UserEntity> findAllByRoleOrderByIdAsc(Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    Optional<UserEntity> findByIdForUpdate(@Param("id") Long id);
}
