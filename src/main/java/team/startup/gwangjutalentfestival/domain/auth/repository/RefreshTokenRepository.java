package team.startup.gwangjutalentfestival.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import team.startup.gwangjutalentfestival.domain.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
