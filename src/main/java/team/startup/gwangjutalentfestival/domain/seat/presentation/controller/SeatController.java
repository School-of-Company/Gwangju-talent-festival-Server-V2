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
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetAllSeatsResponse;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;
import team.startup.gwangjutalentfestival.domain.seat.service.*;

import java.util.List;

/**
 * 좌석 예약, 차단, 조회, SSE 연결 등 좌석 관련 기능을 제공하는 컨트롤러.
 */
@Tag(name = "Seat", description = "좌석 API")
@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
public class SeatController {

    private final ReservationSeatService reservationSeatService;
    private final CancelSeatReservationService cancelSeatReservationService;
    private final PerformerCancelSeatReservationService performerCancelSeatReservationService;
    private final GetCurrentUserSeatService getCurrentUserSeatService;
    private final GetByCurrentPerformerSeatsService getByCurrentPerformerSeatsService;
    private final GetSeatsBySectionService getSeatsBySectionService;
    private final GetAllSeatsService getAllSeatsService;
    private final ConnectSseSeatEventService connectSseSeatEventService;

    /**
     * 좌석을 예약한다.
     *
     * @param reservationSeatRequest 예약할 좌석의 구역 및 번호 정보
     * @return 201 Created
     */
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

    /**
     * 현재 로그인한 사용자의 예약 좌석을 조회한다.
     *
     * @return 예약 좌석의 구역 및 번호
     */
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

    /**
     * 현재 로그인한 공연자가 예약한 좌석 목록을 조회한다.
     *
     * @return 공연자의 예약 좌석 구역 및 번호 목록
     */
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

    /**
     * 특정 구역의 좌석 예약 가능 여부 목록을 조회한다.
     *
     * @param section 조회할 좌석 구역 (A~J)
     * @return 해당 구역의 좌석별 예약 가능 여부 목록
     */
    @Operation(summary = "구역별 좌석 현황 조회", description = "특정 구역의 좌석 예약 가능 여부 목록을 조회합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 좌석 구역"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @GetMapping
    public ResponseEntity<GetSeatsBySectionResponse> getSeatsBySection(
            @RequestParam String section) {
        return ResponseEntity.ok(getSeatsBySectionService.execute(section));
    }

    /**
     * 전체 구역의 좌석 현황을 조회한다.
     *
     * @return 구역별 좌석 예약 가능 여부 맵
     */
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

    /**
     * 좌석 실시간 이벤트를 수신하기 위한 SSE 연결을 생성한다.
     *
     * @return SSE 이미터 객체
     */
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

    /**
     * 현재 로그인한 사용자의 좌석 예약을 취소한다.
     *
     * @return 204 No Content
     */
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

    /**
     * 공연자가 본인이 예약한 특정 좌석을 취소한다.
     *
     * @param request 취소할 좌석의 구역 및 번호 정보
     * @return 204 No Content
     */
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
