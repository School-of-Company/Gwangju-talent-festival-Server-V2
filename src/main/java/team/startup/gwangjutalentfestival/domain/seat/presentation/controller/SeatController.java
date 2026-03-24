package team.startup.gwangjutalentfestival.domain.seat.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;

@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
public class SeatController {

    private final ReservationSeatService reservationSeatService;
    private final CancelSeatReservationService cancelSeatReservationService;
    private final PerformerCancelSeatReservationService performerCancelSeatReservationService;
    private final BanSeatService banSeatService;
    private final CancelSeatBanService cancelSeatBanService;

    @PostMapping
    public ResponseEntity<Void> reservationSeat(
            @Valid @RequestBody ReservationSeatRequest reservationSeatRequest) {
        reservationSeatService.execute(reservationSeatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/ban")
    public ResponseEntity<Void> banSeat(
            @Valid @RequestBody BanSeatRequest banSeatRequest) {
        banSeatService.execute(banSeatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> cancelSeat() {
        cancelSeatReservationService.execute();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/ban")
    public ResponseEntity<Void> cancelBan(
            @Valid @RequestBody CancelSeatBanRequest cancelSeatBanRequest) {
        cancelSeatBanService.execute(cancelSeatBanRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/performer")
    public ResponseEntity<Void> performerCancelSeat(
            @Valid @RequestBody PerformerCancelSeatReservationRequest request) {
        performerCancelSeatReservationService.execute(request);
        return ResponseEntity.noContent().build();
    }
}
