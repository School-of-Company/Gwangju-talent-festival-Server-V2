package team.startup.gwangjutalentfestival.domain.seat.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetAllSeatsResponse;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;
import team.startup.gwangjutalentfestival.domain.seat.service.*;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;

import java.util.List;

@Tag(name = "Seat", description = "좌석 API")
@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
public class SeatController {

    private final ReservationSeatService reservationSeatService;
    private final CancelSeatReservationService cancelSeatReservationService;
    private final PerformerCancelSeatReservationService performerCancelSeatReservationService;
    private final BanSeatService banSeatService;
    private final CancelSeatBanService cancelSeatBanService;
    private final GetCurrentUserSeatService getCurrentUserSeatService;
    private final GetByCurrentPerformerSeatsService getByCurrentPerformerSeatsService;
    private final GetSeatsBySectionService getSeatsBySectionService;
    private final GetAllSeatsService getAllSeatsService;
    private final ConnectSseSeatEventService connectSseSeatEventService;

    @Operation(summary = "좌석 예약", description = "좌석을 예약합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좌석 예약 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 좌석 구역"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "409", description = "이미 예약된 좌석")
    })
    @PostMapping
    public ResponseEntity<Void> reservationSeat(
            @Valid @RequestBody ReservationSeatRequest reservationSeatRequest) {
        reservationSeatService.execute(reservationSeatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "좌석 차단", description = "관리자가 특정 좌석을 차단합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좌석 차단 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 좌석 구역"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "이미 차단된 좌석")
    })
    @PostMapping("/ban")
    public ResponseEntity<Void> banSeat(
            @Valid @RequestBody BanSeatRequest banSeatRequest) {
        banSeatService.execute(banSeatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "내 좌석 조회", description = "현재 로그인한 사용자의 예약 좌석을 조회합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "예약된 좌석 없음")
    })
    @GetMapping("/myself")
    public ResponseEntity<GetSeatResponse> myself() {
        return ResponseEntity.ok(getCurrentUserSeatService.execute());
    }

    @Operation(summary = "공연자 예약 좌석 목록 조회", description = "현재 로그인한 공연자의 예약 좌석 목록을 조회합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/myself/performer")
    public ResponseEntity<List<GetSeatResponse>> getPerformerSeats() {
        return ResponseEntity.ok(getByCurrentPerformerSeatsService.execute());
    }

    @Operation(summary = "구역별 좌석 현황 조회", description = "특정 구역의 좌석 예약 가능 여부 목록을 조회합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 좌석 구역"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @GetMapping
    public ResponseEntity<GetSeatsBySectionResponse> getSeatsBySection(
            @RequestParam Character section) {
        return ResponseEntity.ok(getSeatsBySectionService.execute(section));
    }

    @Operation(summary = "전체 좌석 조회", description = "전체 좌석 현황을 조회합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/all")
    public ResponseEntity<GetAllSeatsResponse> getAllSeats() {
        return ResponseEntity.ok(getAllSeatsService.execute());
    }

    @Operation(summary = "좌석 SSE 연결", description = "좌석 실시간 이벤트를 수신하기 위한 SSE 연결을 맺습니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @GetMapping(value = "/changes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectSse() {
        return connectSseSeatEventService.execute();
    }

    @Operation(summary = "좌석 예약 취소", description = "본인의 좌석 예약을 취소합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좌석 예약 취소 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "예약된 좌석 없음")
    })
    @DeleteMapping
    public ResponseEntity<Void> cancelSeat() {
        cancelSeatReservationService.execute();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "좌석 차단 취소", description = "관리자가 특정 좌석의 차단을 취소합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좌석 차단 취소 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "차단된 좌석 없음")
    })
    @DeleteMapping("/ban")
    public ResponseEntity<Void> cancelBan(
            @Valid @RequestBody CancelSeatBanRequest cancelSeatBanRequest) {
        cancelSeatBanService.execute(cancelSeatBanRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공연자 좌석 예약 취소", description = "공연자가 특정 사용자의 좌석 예약을 취소합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좌석 예약 취소 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "예약된 좌석 없음")
    })
    @DeleteMapping("/performer")
    public ResponseEntity<Void> performerCancelSeat(
            @Valid @RequestBody PerformerCancelSeatReservationRequest request) {
        performerCancelSeatReservationService.execute(request);
        return ResponseEntity.noContent().build();
    }
}
