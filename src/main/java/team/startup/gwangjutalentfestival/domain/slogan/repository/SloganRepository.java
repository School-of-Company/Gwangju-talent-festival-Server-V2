package team.startup.gwangjutalentfestival.domain.slogan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;

public interface SloganRepository extends JpaRepository<SloganEntity, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
}
