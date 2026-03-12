package team.startup.gwangjutalentfestival.domain.slogan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.slogan.entity.Slogan;

public interface SloganRepository extends JpaRepository<Slogan, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
}
