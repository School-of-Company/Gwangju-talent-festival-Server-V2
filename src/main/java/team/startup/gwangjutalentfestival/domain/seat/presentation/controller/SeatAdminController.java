package team.startup.gwangjutalentfestival.domain.seat.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.GetSeatsByPhoneNumberService;

import java.util.List;

/**
 * 좌석 관리자 기능(차단/해제/조회)을 제공하는 컨트롤러.
 */
@Tag(name = "Seat Admin", description = "좌석 관리자 API")
@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
@Validated
public class SeatAdminController {

    private final BanSeatService banSeatService;
    private final CancelSeatBanService cancelSeatBanService;
    private final GetSeatsByPhoneNumberService getSeatsByPhoneNumberService;

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
    public ResponseEntity<Void> banSeat(@Valid @RequestBody BanSeatRequest banSeatRequest) {
        banSeatService.execute(banSeatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    public ResponseEntity<Void> cancelBan(@Valid @RequestBody CancelSeatBanRequest cancelSeatBanRequest) {
        cancelSeatBanService.execute(cancelSeatBanRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * 전화번호로 해당 사용자가 예매한 좌석 목록을 조회한다.
     *
     * @param phoneNumber 조회할 사용자의 전화번호 (010으로 시작하는 11자리)
     * @return 예매 좌석 목록 (예매한 좌석이 없으면 빈 목록)
     */
    @Operation(summary = "전화번호로 예매 좌석 조회", description = "관리자가 전화번호로 해당 사용자의 예매 좌석 목록을 조회합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 전화번호 형식"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 전화번호의 사용자 없음")
    })
    @GetMapping("/search")
    public ResponseEntity<List<GetSeatResponse>> searchByPhoneNumber(
            @RequestParam
            @Pattern(regexp = "^010\\d{8}$", message = "유효한 휴대폰 번호 형식이 아닙니다.")
            String phoneNumber) {
        return ResponseEntity.ok(getSeatsByPhoneNumberService.execute(phoneNumber));
    }
}
