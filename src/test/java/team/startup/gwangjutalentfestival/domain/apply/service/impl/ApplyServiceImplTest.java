package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplyServiceImplTest {

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @Mock
    private ApplyRepository applyRepository;

    @InjectMocks
    private ApplyServiceImpl applyService;

    private static final byte[] VALID_MP4_HEADER = new byte[]{
            0x00, 0x00, 0x00, 0x1C,
            0x66, 0x74, 0x79, 0x70,  // ftyp
            0x69, 0x73, 0x6F, 0x6D   // isom
    };

    @Test
    void 업로드된_파일이_유효한_MP4이면_신청을_저장하고_ID를_반환한다() {
        ApplyRequest request = new ApplyRequest("videos/test-key.mp4", "공연영상.mp4");
        ApplyEntity savedApply = ApplyEntity.builder()
                .id(1L)
                .videoKey("videos/test-key.mp4")
                .originalFilename("공연영상.mp4")
                .build();
        given(awsS3Adapter.readObjectHead(eq("videos/test-key.mp4"), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class))).willReturn(savedApply);

        ApplyResponse response = applyService.execute(request);

        assertThat(response.applyId()).isEqualTo(1L);
        verify(applyRepository).save(argThat(apply ->
                apply.getVideoKey().equals("videos/test-key.mp4")
                        && apply.getOriginalFilename().equals("공연영상.mp4")));
    }

    @Test
    void key가_null이면_InvalidVideoFileException이_발생한다() {
        ApplyRequest request = new ApplyRequest(null, "공연영상.mp4");

        assertThatThrownBy(() -> applyService.execute(request))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void key가_빈_문자열이면_InvalidVideoFileException이_발생한다() {
        ApplyRequest request = new ApplyRequest("  ", "공연영상.mp4");

        assertThatThrownBy(() -> applyService.execute(request))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "uploads/evil.mp4",        // videos/ 경로가 아님
            "videos/../secret.mp4",    // 경로 이탈 시도
            "videos/sub/path.mp4",     // 하위 경로 추가
            "videos/evil.exe",         // 허용되지 않은 확장자
            "videos/.mp4"              // 파일명 비어있음
    })
    void videos_경로_규격을_벗어난_key이면_InvalidVideoFileException이_발생한다(String key) {
        ApplyRequest request = new ApplyRequest(key, "공연영상.mp4");

        assertThatThrownBy(() -> applyService.execute(request))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 업로드된_파일이_MP4_시그니처가_아니면_InvalidVideoFileException이_발생한다() {
        ApplyRequest request = new ApplyRequest("videos/fake-key.mp4", "fake.mp4");
        byte[] notMp4 = new byte[]{
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(notMp4);

        assertThatThrownBy(() -> applyService.execute(request))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 읽어온_헤더가_12바이트_미만이면_InvalidVideoFileException이_발생한다() {
        ApplyRequest request = new ApplyRequest("videos/short-key.mp4", "short.mp4");
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(new byte[]{0x00, 0x01});

        assertThatThrownBy(() -> applyService.execute(request))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 파일명이_없으면_기본_파일명으로_저장한다() {
        ApplyRequest request = new ApplyRequest("videos/test-key.mp4", null);
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        applyService.execute(request);

        verify(applyRepository).save(argThat(apply ->
                apply.getOriginalFilename().equals("video.mp4")));
    }

    @Test
    void 파일명이_255자를_초과하면_잘라서_저장한다() {
        String longName = "가".repeat(300) + ".mp4";
        ApplyRequest request = new ApplyRequest("videos/test-key.mp4", longName);
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        applyService.execute(request);

        verify(applyRepository).save(argThat(apply ->
                apply.getOriginalFilename().length() == 255));
    }
}
