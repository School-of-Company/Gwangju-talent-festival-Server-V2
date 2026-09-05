package team.startup.gwangjutalentfestival.domain.judge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.exception.JudgeCommentTooLargeException;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgeProfileRequest;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeProfileResponse;
import team.startup.gwangjutalentfestival.domain.judge.properties.JudgeStrokesProperties;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeProfileRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JudgeProfileService {

    private final UserUtil userUtil;
    private final JudgeProfileRepository judgeProfileRepository;
    private final ObjectMapper objectMapper;
    private final JudgeStrokesProperties judgeStrokesProperties;

    @Transactional(readOnly = true)
    public GetJudgeProfileResponse get() {
        UserEntity user = userUtil.getCurrentUserRef();
        return judgeProfileRepository.findByUser(user)
                .map(profile -> new GetJudgeProfileResponse(
                        strokesOrEmpty(profile.getAffiliationStrokes()),
                        strokesOrEmpty(profile.getPositionStrokes()),
                        strokesOrEmpty(profile.getNameStrokes())))
                .orElseGet(() -> new GetJudgeProfileResponse(
                        objectMapper.createArrayNode(),
                        objectMapper.createArrayNode(),
                        objectMapper.createArrayNode()));
    }

    @Transactional
    public void save(SaveJudgeProfileRequest request) {
        UserEntity user = userUtil.getCurrentUserRef();
        String affiliationStrokes = serialize(request.affiliationStrokes());
        String positionStrokes = serialize(request.positionStrokes());
        String nameStrokes = serialize(request.nameStrokes());

        judgeProfileRepository.upsert(user.getId(), affiliationStrokes, positionStrokes, nameStrokes);
    }

    private JsonNode strokesOrEmpty(JsonNode strokes) {
        return strokes == null || strokes.isNull() ? objectMapper.createArrayNode() : strokes;
    }

    private String serialize(JsonNode strokes) {
        try {
            String value = objectMapper.writeValueAsString(strokes);
            if (value.getBytes(StandardCharsets.UTF_8).length > judgeStrokesProperties.maxBytes()) {
                throw new JudgeCommentTooLargeException();
            }
            return value;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("strokes 데이터를 직렬화할 수 없습니다.", e);
        }
    }
}
