package team.startup.gwangjutalentfestival.domain.seat.repository.custom.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.seat.entity.QSeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatBanCustomRepository;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SeatBanCustomRepositoryImpl implements SeatBanCustomRepository {

    private final JPAQueryFactory queryFactory;

    private static final QSeatBanEntity seatBan = QSeatBanEntity.seatBanEntity;

    @Override
    public List<SectionSeatNumber> findSeatNumbersByRole(Role role) {
        return queryFactory
                .select(Projections.constructor(
                        SectionSeatNumber.class,
                        seatBan.seatSection,
                        seatBan.seatNumber
                ))
                .from(seatBan)
                .where(seatBan.role.eq(role))
                .fetch();
    }

    @Override
    public Set<Integer> findSeatNumbersBySeatSectionAndRole(Character seatSection, Role role) {
        return new HashSet<>(queryFactory
                .select(seatBan.seatNumber)
                .from(seatBan)
                .where(seatBan.seatSection.eq(seatSection).and(seatBan.role.eq(role)))
                .fetch());
    }
}
