package team.startup.gwangjutalentfestival.domain.seat.repository.custom.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.seat.entity.QSeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatBanCustomRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link SeatBanCustomRepository}의 QueryDSL 구현체.
 */
@Repository
@RequiredArgsConstructor
public class SeatBanCustomRepositoryImpl implements SeatBanCustomRepository {

    private final JPAQueryFactory queryFactory;

    private static final QSeatBanEntity seatBan = QSeatBanEntity.seatBanEntity;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SectionSeatNumber> findAllSeatNumbers() {
        return queryFactory
                .select(Projections.constructor(
                        SectionSeatNumber.class,
                        seatBan.seatSection,
                        seatBan.seatNumber
                ))
                .from(seatBan)
                .fetch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> findSeatNumbersBySeatSection(String seatSection) {
        return new HashSet<>(queryFactory
                .select(seatBan.seatNumber)
                .from(seatBan)
                .where(seatBan.seatSection.eq(seatSection))
                .fetch());
    }
}
