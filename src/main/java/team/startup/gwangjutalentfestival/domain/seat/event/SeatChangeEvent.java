package team.startup.gwangjutalentfestival.domain.seat.event;

public record SeatChangeEvent(
        String seatSection,
        Integer seatNumber,
        Boolean isAvailable
) {
}
