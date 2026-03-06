package team.startup.gwangjutalentfestival.auth.repository;

import org.springframework.data.repository.CrudRepository;
import team.startup.gwangjutalentfestival.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
