package team.startup.gwangjutalentfestival.domain.seat.presentation.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.event.handler.SeatChangeEventListener;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetAllSeatsService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetByCurrentPerformerSeatsService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetCurrentUserSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetSeatsBySectionService;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.impl.ConnectSseSeatEventServiceImpl;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;
import team.startup.gwangjutalentfestival.global.sse.SeatSseEmitterManager;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeatSseStreamingTest {

    private MockMvc mockMvc;
    private SeatSseEmitterManager emitterManager;
    private SeatChangeEventListener eventListener;

    @BeforeEach
    void setUp() {
        emitterManager = new SeatSseEmitterManager();
        TaskScheduler taskScheduler = mock(TaskScheduler.class);
        ScheduledFuture<?> heartbeat = mock(ScheduledFuture.class);
        doReturn(heartbeat).when(taskScheduler)
                .scheduleAtFixedRate(any(Runnable.class), any(Duration.class));

        ConnectSseSeatEventServiceImpl connectService =
                new ConnectSseSeatEventServiceImpl(emitterManager, taskScheduler);
        eventListener = new SeatChangeEventListener(emitterManager);

        SeatController controller = new SeatController(
                mock(ReservationSeatService.class),
                mock(CancelSeatReservationService.class),
                mock(PerformerCancelSeatReservationService.class),
                mock(GetCurrentUserSeatService.class),
                mock(GetByCurrentPerformerSeatsService.class),
                mock(GetSeatsBySectionService.class),
                mock(GetAllSeatsService.class),
                connectService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        CustomUserDetails user = CustomUserDetails.fromToken(1L, Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        emitterManager.getAllEmitters().forEach(emitter -> emitter.complete());
        SecurityContextHolder.clearContext();
    }

    @Test
    void 연결_후_좌석_변경_이벤트를_같은_스트림으로_즉시_전송한다() throws Exception {
        MvcResult result = mockMvc.perform(get("/seat/changes"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        eventListener.execute(new SeatChangeEvent("A", 16, false));
        emitterManager.getAllEmitters().forEach(emitter -> emitter.complete());

        mockMvc.perform(asyncDispatch(result))
                .andExpect(header().string("Cache-Control", "no-cache, no-transform"))
                .andExpect(header().string("X-Accel-Buffering", "no"))
                .andExpect(content().string(containsString("event:connected")))
                .andExpect(content().string(containsString("event:SEAT_CHANGE")))
                .andExpect(content().string(containsString(
                        "data:{\"seatSection\":\"A\",\"seatNumber\":16,\"isAvailable\":false}")));
    }
}
