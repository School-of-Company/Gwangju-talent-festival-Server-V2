package team.startup.gwangjutalentfestival.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCodeCount;

@Repository
public interface VerifyCodeCountRepository extends CrudRepository<VerifyCodeCount, String> {
}
