package team.startup.gwangjutalentfestival.domain.seat.presentation.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BulkReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ConnectSseSeatEventService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetAllSeatsService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetByCurrentPerformerSeatsService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetCurrentUserSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.GetSeatsBySectionService;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.global.exception.HttpExceptionHandler;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SeatControllerBulkReservationTest {

    @Mock
    private ReservationSeatService reservationSeatService;
    @Mock
    private CancelSeatReservationService cancelSeatReservationService;
    @Mock
    private PerformerCancelSeatReservationService performerCancelSeatReservationService;
    @Mock
    private GetCurrentUserSeatService getCurrentUserSeatService;
    @Mock
    private GetByCurrentPerformerSeatsService getByCurrentPerformerSeatsService;
    @Mock
    private GetSeatsBySectionService getSeatsBySectionService;
    @Mock
    private GetAllSeatsService getAllSeatsService;
    @Mock
    private ConnectSseSeatEventService connectSseSeatEventService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        SeatController controller = new SeatController(
                reservationSeatService,
                cancelSeatReservationService,
                performerCancelSeatReservationService,
                getCurrentUserSeatService,
                getByCurrentPerformerSeatsService,
                getSeatsBySectionService,
                getAllSeatsService,
                connectSseSeatEventService
        );
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new HttpExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void 두_좌석_예약은_201과_요청_순서의_좌석_목록을_반환한다() throws Exception {
        given(reservationSeatService.executeBulk(argThat(request -> request.seats().size() == 2)))
                .willReturn(List.of(new GetSeatResponse("A", 17), new GetSeatResponse("A", 16)));

        mockMvc.perform(post("/seat/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"seats":[
                                  {"seatSection":"A","seatNumber":17},
                                  {"seatSection":"A","seatNumber":16}
                                ]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].seatSection").value("A"))
                .andExpect(jsonPath("$[0].seatNumber").value(17))
                .andExpect(jsonPath("$[1].seatSection").value("A"))
                .andExpect(jsonPath("$[1].seatNumber").value(16));

        verify(reservationSeatService).executeBulk(argThat(request ->
                request.equals(new BulkReservationSeatRequest(List.of(
                        new ReservationSeatRequest("A", 17),
                        new ReservationSeatRequest("A", 16))))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"seats\":null}",
            "{\"seats\":[]}",
            "{\"seats\":[{\"seatSection\":\"A\",\"seatNumber\":16},{\"seatSection\":\"A\",\"seatNumber\":17},{\"seatSection\":\"A\",\"seatNumber\":18}]}",
            "{\"seats\":[null]}",
            "{\"seats\":[{\"seatSection\":\"Z\",\"seatNumber\":0}]}"
    })
    void 유효하지_않은_다중_예약_요청은_400이고_서비스를_호출하지_않는다(String body) throws Exception {
        mockMvc.perform(post("/seat/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationSeatService);
    }
}
