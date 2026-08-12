package team.startup.gwangjutalentfestival.domain.seat.presentation.data.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BulkReservationSeatRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void 한_좌석과_두_좌석_요청은_유효하다() {
        BulkReservationSeatRequest oneSeat = new BulkReservationSeatRequest(List.of(
                new ReservationSeatRequest("A", 16)));
        BulkReservationSeatRequest twoSeats = new BulkReservationSeatRequest(List.of(
                new ReservationSeatRequest("A", 16),
                new ReservationSeatRequest("A", 17)));

        assertThat(validator.validate(oneSeat)).isEmpty();
        assertThat(validator.validate(twoSeats)).isEmpty();
    }

    @Test
    void 좌석_목록이_null이거나_비어있거나_세_개면_거부한다() {
        assertThat(validator.validate(new BulkReservationSeatRequest(null))).isNotEmpty();
        assertThat(validator.validate(new BulkReservationSeatRequest(List.of()))).isNotEmpty();
        assertThat(validator.validate(new BulkReservationSeatRequest(List.of(
                new ReservationSeatRequest("A", 16),
                new ReservationSeatRequest("A", 17),
                new ReservationSeatRequest("A", 18))))).isNotEmpty();
    }

    @Test
    void 내부_좌석의_형식도_연쇄_검증한다() {
        BulkReservationSeatRequest request = new BulkReservationSeatRequest(List.of(
                new ReservationSeatRequest("Z", 0)));

        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void 내부_좌석의_필수값이_null이면_거부한다() {
        BulkReservationSeatRequest request = new BulkReservationSeatRequest(List.of(
                new ReservationSeatRequest(null, null)));

        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void null_좌석_요소를_거부한다() {
        List<ReservationSeatRequest> seats = new ArrayList<>();
        seats.add(null);

        assertThat(validator.validate(new BulkReservationSeatRequest(seats))).isNotEmpty();
    }

    @Test
    void 생성_후_요청_목록을_변경할_수_없다() {
        BulkReservationSeatRequest request = new BulkReservationSeatRequest(new ArrayList<>(List.of(
                new ReservationSeatRequest("A", 16))));

        assertThatThrownBy(() -> request.seats().add(new ReservationSeatRequest("A", 17)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
