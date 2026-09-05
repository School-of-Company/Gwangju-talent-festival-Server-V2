package team.startup.gwangjutalentfestival.domain.judge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeProfileEntity;
import team.startup.gwangjutalentfestival.domain.judge.exception.JudgeCommentTooLargeException;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgeProfileRequest;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeProfileResponse;
import team.startup.gwangjutalentfestival.domain.judge.properties.JudgeStrokesProperties;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeProfileRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JudgeProfileServiceTest {

    private static final int MAX_STROKES_BYTES = 20_971_520;

    @Mock
    private UserUtil userUtil;

    @Mock
    private JudgeProfileRepository judgeProfileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JudgeProfileService service;
    private UserEntity judge;

    @BeforeEach
    void setUp() {
        service = new JudgeProfileService(userUtil, judgeProfileRepository, objectMapper,
                new JudgeStrokesProperties(MAX_STROKES_BYTES));
        judge = UserEntity.builder().id(1L).role(Role.JUDGE).build();
        given(userUtil.getCurrentUserRef()).willReturn(judge);
    }

    @Test
    void 소속_직위_이름_필기를_한번에_upsert한다() {
        SaveJudgeProfileRequest request = new SaveJudgeProfileRequest(
                strokes("소속"), strokes("직위"), strokes("이름"));

        service.save(request);

        ArgumentCaptor<String> affiliation = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> position = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        verify(judgeProfileRepository).upsert(
                eq(1L), affiliation.capture(), position.capture(), name.capture());
        assertThat(affiliation.getValue()).contains("소속");
        assertThat(position.getValue()).contains("직위");
        assertThat(name.getValue()).contains("이름");
    }

    @Test
    void 저장된_필기_정보를_조회한다() {
        given(judgeProfileRepository.findByUser(judge)).willReturn(Optional.of(
                JudgeProfileEntity.builder()
                        .user(judge)
                        .affiliationStrokes(strokes("소속"))
                        .positionStrokes(strokes("직위"))
                        .nameStrokes(strokes("이름"))
                        .build()));

        GetJudgeProfileResponse response = service.get();

        assertThat(response.affiliationStrokes().get(0).path("value").asText()).isEqualTo("소속");
        assertThat(response.positionStrokes().get(0).path("value").asText()).isEqualTo("직위");
        assertThat(response.nameStrokes().get(0).path("value").asText()).isEqualTo("이름");
    }

    @Test
    void 저장된_정보가_없으면_세_필드_모두_빈_배열을_반환한다() {
        given(judgeProfileRepository.findByUser(judge)).willReturn(Optional.empty());

        GetJudgeProfileResponse response = service.get();

        assertThat(response.affiliationStrokes()).isEmpty();
        assertThat(response.positionStrokes()).isEmpty();
        assertThat(response.nameStrokes()).isEmpty();
    }

    @Test
    void 각_필기는_빈_배열로_초기화할_수_있다() {
        ArrayNode empty = objectMapper.createArrayNode();

        service.save(new SaveJudgeProfileRequest(empty, empty, empty));

        verify(judgeProfileRepository).upsert(1L, "[]", "[]", "[]");
    }

    @Test
    void 필기_하나가_크기_제한을_초과하면_저장하지_않는다() {
        ArrayNode large = objectMapper.createArrayNode();
        large.addObject().put("data", "a".repeat(MAX_STROKES_BYTES + 1));

        assertThatThrownBy(() -> service.save(new SaveJudgeProfileRequest(
                large, objectMapper.createArrayNode(), objectMapper.createArrayNode())))
                .isInstanceOf(JudgeCommentTooLargeException.class);
    }

    private ArrayNode strokes(String value) {
        ArrayNode strokes = objectMapper.createArrayNode();
        strokes.addObject().put("value", value);
        return strokes;
    }
}
