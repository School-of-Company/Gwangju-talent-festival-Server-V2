package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaveJudgeCommentRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void strokes가_배열이_아닌_객체이면_역직렬화에_실패한다() {
        assertThatThrownBy(() -> objectMapper.readValue(
                "{\"strokes\":{\"x\":1}}", SaveJudgeCommentRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void strokes가_배열이_아닌_문자열이면_역직렬화에_실패한다() {
        assertThatThrownBy(() -> objectMapper.readValue(
                "{\"strokes\":\"invalid\"}", SaveJudgeCommentRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void strokes가_배열이면_정상적으로_역직렬화된다() throws Exception {
        SaveJudgeCommentRequest request = objectMapper.readValue(
                "{\"strokes\":[{\"x\":1}]}", SaveJudgeCommentRequest.class);

        assertThat(request.strokes().isArray()).isTrue();
        assertThat(request.strokes().get(0).get("x").asInt()).isEqualTo(1);
    }
}