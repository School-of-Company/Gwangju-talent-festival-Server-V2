package team.startup.gwangjutalentfestival.global.security;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import team.startup.gwangjutalentfestival.domain.auth.presentation.controller.AuthController;
import team.startup.gwangjutalentfestival.domain.auth.repository.TokenBlacklistRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.LoginService;
import team.startup.gwangjutalentfestival.domain.auth.service.LogoutService;
import team.startup.gwangjutalentfestival.domain.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.auth.service.SendVerifyCodeService;
import team.startup.gwangjutalentfestival.domain.auth.service.SignUpService;
import team.startup.gwangjutalentfestival.domain.excel.controller.ExcelController;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgeSheetsService;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;
import team.startup.gwangjutalentfestival.domain.judge.presentation.controller.JudgeController;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeEventService;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeMonitoringService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetAllJudgementService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeCommentService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgementService;
import team.startup.gwangjutalentfestival.domain.judge.service.JudgeProfileService;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgeCommentService;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgementScoreService;
import team.startup.gwangjutalentfestival.domain.monitoring.client.DiscordWebhookClient;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.controller.MonitoringController;
import team.startup.gwangjutalentfestival.domain.monitoring.service.CreateIncidentFeedbackService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.ExportDatasetService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventDetailService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventListService;
import team.startup.gwangjutalentfestival.domain.monitoring.service.GetAnomalyEventSummaryService;
import team.startup.gwangjutalentfestival.domain.performer.presentation.controller.PerformerController;
import team.startup.gwangjutalentfestival.domain.performer.service.VerifyPerformerService;
import team.startup.gwangjutalentfestival.domain.seat.presentation.controller.SeatController;
import team.startup.gwangjutalentfestival.domain.seat.presentation.controller.SeatAdminController;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ConnectSseSeatEventService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetAllSeatsService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetByCurrentPerformerSeatsService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetCurrentUserSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetSeatsBySectionService;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.GetSeatsByPhoneNumberService;
import team.startup.gwangjutalentfestival.domain.team.presentation.controller.TeamController;
import team.startup.gwangjutalentfestival.domain.team.service.GetAllTeamService;
import team.startup.gwangjutalentfestival.domain.team.service.GetTeamRankingService;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamOrderService;
import team.startup.gwangjutalentfestival.domain.team.service.UpdateTeamPerformanceService;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;
import team.startup.gwangjutalentfestival.global.security.config.SecurityConfig;
import team.startup.gwangjutalentfestival.global.security.filter.JwtFilter;
import team.startup.gwangjutalentfestival.global.security.handler.JwtAccessDeniedHandler;
import team.startup.gwangjutalentfestival.global.security.handler.JwtAuthenticationEntryPoint;
import team.startup.gwangjutalentfestival.global.security.properties.CorsProperties;

import java.util.EnumSet;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * USER, ADMIN, JUDGE, PERFORMER 역할별 API 접근 허용 상태를 회귀 테스트로 고정한다.
 * <p>실제 {@link SecurityConfig}, {@link JwtFilter}를 통과시켜 인가 규칙을 검증하며,
 * 도메인 서비스는 인가 통과 이후 동작에 영향받지 않도록 Mock으로 대체한다.</p>
 */
@WebMvcTest(controllers = {
        TeamController.class,
        SeatController.class,
        SeatAdminController.class,
        JudgeController.class,
        MonitoringController.class,
        ExcelController.class,
        AuthController.class,
        PerformerController.class
})
@Import({SecurityConfig.class, JwtFilter.class, JwtProvider.class, JwtAccessDeniedHandler.class, JwtAuthenticationEntryPoint.class})
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-junit-testing-only-32bytes!!",
        "jwt.access-token-expiration=180000000",
        "jwt.refresh-token-expiration=1209600",
        "cors.allowed-origins=http://localhost:3000"
})
class RoleBasedApiAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private GetAllTeamService getAllTeamService;
    @MockBean
    private GetTeamRankingService getTeamRankingService;
    @MockBean
    private UpdateTeamPerformanceService updateTeamPerformanceService;
    @MockBean
    private UpdateTeamOrderService updateTeamOrderService;

    @MockBean
    private ReservationSeatService reservationSeatService;
    @MockBean
    private CancelSeatReservationService cancelSeatReservationService;
    @MockBean
    private PerformerCancelSeatReservationService performerCancelSeatReservationService;
    @MockBean
    private GetCurrentUserSeatService getCurrentUserSeatService;
    @MockBean
    private GetByCurrentPerformerSeatsService getByCurrentPerformerSeatsService;
    @MockBean
    private GetSeatsBySectionService getSeatsBySectionService;
    @MockBean
    private GetAllSeatsService getAllSeatsService;
    @MockBean
    private ConnectSseSeatEventService connectSseSeatEventService;
    @MockBean
    private BanSeatService banSeatService;
    @MockBean
    private CancelSeatBanService cancelSeatBanService;
    @MockBean
    private GetSeatsByPhoneNumberService getSeatsByPhoneNumberService;

    @MockBean
    private SaveJudgementScoreService saveJudgementScoreService;
    @MockBean
    private GetAllJudgementService getAllJudgementService;
    @MockBean
    private GetJudgementService getJudgementService;
    @MockBean
    private ConnectSseJudgeEventService connectSseJudgeEventService;
    @MockBean
    private ConnectSseJudgeMonitoringService connectSseJudgeMonitoringService;
    @MockBean
    private GetJudgeCommentService getJudgeCommentService;
    @MockBean
    private SaveJudgeCommentService saveJudgeCommentService;
    @MockBean
    private JudgeProfileService judgeProfileService;

    @MockBean
    private GetAnomalyEventListService getAnomalyEventListService;
    @MockBean
    private GetAnomalyEventDetailService getAnomalyEventDetailService;
    @MockBean
    private CreateIncidentFeedbackService createIncidentFeedbackService;
    @MockBean
    private GetAnomalyEventSummaryService getAnomalyEventSummaryService;
    @MockBean
    private ExportDatasetService exportDatasetService;
    @MockBean
    private DiscordWebhookClient discordWebhookClient;

    @MockBean
    private DownloadJudgingSummaryExcelService downloadJudgingSummaryExcelService;
    @MockBean
    private DownloadJudgeSheetsService downloadJudgeSheetsService;

    @MockBean
    private SendVerifyCodeService sendVerifyCodeService;
    @MockBean
    private SignUpService signUpService;
    @MockBean
    private LoginService loginService;
    @MockBean
    private LogoutService logoutService;
    @MockBean
    private ReissueTokenService reissueTokenService;
    @MockBean
    private VerifyPerformerService verifyPerformerService;
    @MockBean
    private TokenBlacklistRepository tokenBlacklistRepository;
    @MockBean
    private StringRedisTemplate stringRedisTemplate;
    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUpRateLimit() {
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);
    }

    private record Endpoint(
            String description,
            HttpMethod method,
            String path,
            String[] params,
            String jsonBody,
            int successStatus,
            Set<Role> allowedRoles
    ) {
        MockHttpServletRequestBuilder request() {
            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, path);
            for (int i = 0; i + 1 < params.length; i += 2) {
                builder.param(params[i], params[i + 1]);
            }
            if (jsonBody != null) {
                builder.contentType(MediaType.APPLICATION_JSON).content(jsonBody);
            }
            return builder;
        }
    }

    private static final List<Endpoint> ENDPOINTS = List.of(
            new Endpoint("팀 공연 순서 변경", HttpMethod.PATCH, "/team/order", new String[0],
                    "{\"orderItems\":[{\"teamId\":1,\"order\":1}]}", 204, Set.of(Role.ADMIN)),
            new Endpoint("좌석 예약", HttpMethod.POST, "/seat", new String[0],
                    "{\"seatSection\":\"A\",\"seatNumber\":1}", 201, Set.of(Role.USER, Role.PERFORMER)),
            new Endpoint("참가자 다중 좌석 예약", HttpMethod.POST, "/seat/bulk", new String[0],
                    "{\"seats\":[{\"seatSection\":\"A\",\"seatNumber\":16},{\"seatSection\":\"A\",\"seatNumber\":17}]}",
                    201, Set.of(Role.PERFORMER)),
            new Endpoint("좌석 차단", HttpMethod.POST, "/seat/ban", new String[0],
                    "{\"seatSection\":\"A\",\"seatNumber\":1}", 201, Set.of(Role.ADMIN)),
            new Endpoint("좌석 차단 해제", HttpMethod.DELETE, "/seat/ban", new String[0],
                    "{\"seatSection\":\"A\",\"seatNumber\":1}", 204, Set.of(Role.ADMIN)),
            new Endpoint("구역별 좌석 조회", HttpMethod.GET, "/seat", new String[]{"section", "A"}, null, 200,
                    Set.of(Role.ADMIN, Role.USER, Role.PERFORMER)),
            new Endpoint("내 좌석 조회", HttpMethod.GET, "/seat/myself", new String[0], null, 200,
                    Set.of(Role.ADMIN, Role.USER)),
            new Endpoint("공연자 예약 좌석 조회", HttpMethod.GET, "/seat/myself/performer", new String[0], null, 200,
                    Set.of(Role.PERFORMER)),
            new Endpoint("전체 좌석 조회", HttpMethod.GET, "/seat/all", new String[0], null, 200,
                    Set.of(Role.ADMIN, Role.USER, Role.PERFORMER)),
            new Endpoint("좌석 예약 취소", HttpMethod.DELETE, "/seat", new String[0], null, 204,
                    Set.of(Role.ADMIN, Role.USER)),
            new Endpoint("전화번호로 예매 좌석 조회", HttpMethod.GET, "/seat/search",
                    new String[]{"phoneNumber", "01012345678"}, null, 200, Set.of(Role.ADMIN)),
            new Endpoint("심사위원 전체 심사 조회", HttpMethod.GET, "/judge", new String[0], null, 200,
                    Set.of(Role.JUDGE)),
            new Endpoint("이상 탐지 요약 조회", HttpMethod.GET, "/monitoring/anomalies/summary", new String[0], null, 200,
                    Set.of(Role.ADMIN)),
            new Endpoint("심사 집계표 다운로드", HttpMethod.GET, "/excel/summary", new String[0], null, 200,
                    Set.of(Role.ADMIN)),
            new Endpoint("심사위원별 심사표 다운로드", HttpMethod.GET, "/excel/judge-sheets", new String[0], null, 200,
                    Set.of(Role.ADMIN)),
            new Endpoint("로그아웃", HttpMethod.DELETE, "/auth/logout", new String[0], null, 204,
                    EnumSet.allOf(Role.class)),
            new Endpoint("출연진 인증", HttpMethod.POST, "/performer/verify", new String[0],
                    "{\"name\":\"홍길동\",\"code\":\"verification-code\"}", 200, Set.of(Role.USER))
    );

    @TestFactory
    Stream<DynamicTest> 역할별_API_접근_허용_상태가_고정된다() {
        return ENDPOINTS.stream().flatMap(endpoint ->
                Stream.concat(
                        Stream.of(DynamicTest.dynamicTest(
                                endpoint.description() + " - 토큰 없이 요청하면 401을 반환한다",
                                () -> mockMvc.perform(endpoint.request())
                                        .andExpect(status().isUnauthorized())
                        )),
                        Stream.of(Role.values()).map(role ->
                                DynamicTest.dynamicTest(
                                        endpoint.description() + " - " + role + " 역할로 요청하면 "
                                                + (endpoint.allowedRoles().contains(role)
                                                ? endpoint.successStatus() + "을 반환한다"
                                                : "403을 반환한다"),
                                        () -> {
                                            String token = jwtProvider.generateAccessToken(1L, role);
                                            var result = mockMvc.perform(endpoint.request()
                                                    .header("Authorization", "Bearer " + token));

                                            if (endpoint.allowedRoles().contains(role)) {
                                                result.andExpect(status().is(endpoint.successStatus()));
                                            } else {
                                                result.andExpect(status().isForbidden());
                                            }
                                        }
                                )
                        )
                )
        );
    }

    @Test
    void ADMIN이_W구역을_차단하면_400을_반환한다() throws Exception {
        String token = jwtProvider.generateAccessToken(1L, Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.post("/seat/ban")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatSection\":\"W\",\"seatNumber\":1}"))
                .andExpect(status().isBadRequest());
    }
}
