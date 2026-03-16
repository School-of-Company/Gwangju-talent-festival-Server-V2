package team.startup.gwangjutalentfestival.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;

@Repository
public interface VerifyCodeRepository extends CrudRepository<VerifyCode, String> {
}
