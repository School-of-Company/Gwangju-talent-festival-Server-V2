package team.startup.gwangjutalentfestival.domain.judge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.judge.service.impl.ConnectSseJudgeEventServiceImpl;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.sse.JudgeSseEmitterManager;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectSseJudgeEventServiceTest {

    @Mock
    private JudgeSseEmitterManager judgeSseEmitterManager;

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private ConnectSseJudgeEventServiceImpl connectSseJudgeEventService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void 정상_연결_시_SseEmitter가_반환된다() {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgeSseEmitterManager.addEmitter(1L)).willReturn(mockEmitter);

        SseEmitter result = connectSseJudgeEventService.execute();

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(mockEmitter);
    }

    @Test
    void 연결_시_manager에_emitter가_등록된다() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgeSseEmitterManager.addEmitter(1L)).willReturn(mockEmitter);

        connectSseJudgeEventService.execute();

        verify(judgeSseEmitterManager).addEmitter(1L);
    }

    @Test
    void 연결_시_connected_이벤트가_전송된다() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgeSseEmitterManager.addEmitter(1L)).willReturn(mockEmitter);

        connectSseJudgeEventService.execute();

        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void connected_이벤트_전송_중_IOException_발생_시_completeWithError가_호출된다() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgeSseEmitterManager.addEmitter(1L)).willReturn(mockEmitter);
        doThrow(new IOException("연결 오류")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        SseEmitter result = connectSseJudgeEventService.execute();

        assertThat(result).isSameAs(mockEmitter);
        verify(mockEmitter).completeWithError(any(IOException.class));
    }

    @Test
    void onCompletion_onTimeout_onError_콜백이_등록된다() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgeSseEmitterManager.addEmitter(1L)).willReturn(mockEmitter);

        connectSseJudgeEventService.execute();

        verify(mockEmitter, atLeastOnce()).onCompletion(any());
        verify(mockEmitter, atLeastOnce()).onTimeout(any());
        verify(mockEmitter, atLeastOnce()).onError(any());
    }
}
