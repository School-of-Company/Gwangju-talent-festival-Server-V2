package team.startup.gwangjutalentfestival.domain.judge.presentation.data.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaveJudgeProfileRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 세_필기값을_배열로_역직렬화한다() throws Exception {
        SaveJudgeProfileRequest request = objectMapper.readValue("""
                {
                  "affiliationStrokes": [{"value": "소속"}],
                  "positionStrokes": [],
                  "nameStrokes": [{"value": "이름"}]
                }
                """, SaveJudgeProfileRequest.class);

        assertThat(request.affiliationStrokes()).hasSize(1);
        assertThat(request.positionStrokes()).isEmpty();
        assertThat(request.nameStrokes()).hasSize(1);
    }

    @Test
    void 필기값이_배열이_아니면_역직렬화에_실패한다() {
        assertThatThrownBy(() -> objectMapper.readValue("""
                {
                  "affiliationStrokes": {},
                  "positionStrokes": [],
                  "nameStrokes": []
                }
                """, SaveJudgeProfileRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }
}
