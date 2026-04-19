package team.startup.gwangjutalentfestival.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;

/**
 * Redis에 저장된 {@link team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode}를 관리하는 레포지토리.
 */
@Repository
public interface VerifyCodeRepository extends CrudRepository<VerifyCode, String> {
}
