package team.startup.gwangjutalentfestival.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;

/**
 * Redis에 저장된 {@link team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken}을 관리하는 레포지토리.
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
