package team.startup.gwangjutalentfestival.domain.seat.repository.custom.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.seat.entity.QSeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.SectionSeatNumber;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatReservationCustomRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SeatReservationCustomRepositoryImpl implements SeatReservationCustomRepository {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    private static final QSeatEntity seat = QSeatEntity.seatEntity;

    @Override
    public List<SectionSeatNumber> findAllSeatNumbers() {
        return queryFactory
                .select(Projections.constructor(
                        SectionSeatNumber.class,
                        seat.seatSection,
                        seat.seatNumber
                ))
                .from(seat)
                .fetch();
    }

    @Override
    public Set<Integer> findSeatNumbersBySeatSection(String seatSection) {
        return new HashSet<>(queryFactory
                .select(seat.seatNumber)
                .from(seat)
                .where(seat.seatSection.eq(seatSection))
                .fetch());
    }

    @Override
    public int checkAvailability(String seatSection, Integer seatNumber) {
        Object result = entityManager.createNativeQuery(
                "SELECT EXISTS(SELECT 1 FROM seat WHERE seat_section = :section AND seat_number = :number)" +
                " + EXISTS(SELECT 1 FROM seat_ban WHERE seat_section = :section AND seat_number = :number) * 2"
        ).setParameter("section", seatSection)
         .setParameter("number", seatNumber)
         .getSingleResult();
        return ((Number) result).intValue();
    }
}
