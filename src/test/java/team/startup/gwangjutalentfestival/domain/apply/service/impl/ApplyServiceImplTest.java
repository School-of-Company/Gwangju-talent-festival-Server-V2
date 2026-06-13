package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyCompleteRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyPartInput;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
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

    private static ApplyCompleteRequest request(String key, String uploadId, String filename) {
        return new ApplyCompleteRequest(key, uploadId, filename, List.of(new ApplyPartInput(1, "etag1")));
    }

    @Test
    void 유효한_완료_요청이면_멀티파트를_완료하고_신청을_저장한다() {
        ApplyCompleteRequest req = request("videos/test-key.mp4", "upload-id", "공연영상.mp4");
        ApplyEntity saved = ApplyEntity.builder()
                .id(1L).videoKey("videos/test-key.mp4").originalFilename("공연영상.mp4").build();
        given(awsS3Adapter.readObjectHead(eq("videos/test-key.mp4"), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class))).willReturn(saved);

        ApplyResponse response = applyService.execute(req);

        assertThat(response.applyId()).isEqualTo(1L);
        verify(awsS3Adapter).completeMultipartUpload(eq("videos/test-key.mp4"), eq("upload-id"), anyList());
        verify(applyRepository).save(argThat(a ->
                a.getVideoKey().equals("videos/test-key.mp4") && a.getOriginalFilename().equals("공연영상.mp4")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"uploads/evil.mp4", "videos/../secret.mp4", "videos/sub/path.mp4", "videos/evil.exe"})
    void videos_경로_규격을_벗어난_key이면_InvalidVideoFileException이_발생한다(String key) {
        assertThatThrownBy(() -> applyService.execute(request(key, "upload-id", "x.mp4")))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void uploadId가_없으면_InvalidVideoFileException이_발생한다() {
        assertThatThrownBy(() -> applyService.execute(request("videos/key.mp4", "  ", "x.mp4")))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 파트_목록이_비어있으면_InvalidVideoFileException이_발생한다() {
        ApplyCompleteRequest req = new ApplyCompleteRequest("videos/key.mp4", "upload-id", "x.mp4", List.of());
        assertThatThrownBy(() -> applyService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 완료된_파일이_MP4_시그니처가_아니면_S3객체를_삭제하고_InvalidVideoFileException이_발생한다() {
        ApplyCompleteRequest req = request("videos/key.mp4", "upload-id", "x.mp4");
        byte[] notMp4 = new byte[12];
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(notMp4);

        assertThatThrownBy(() -> applyService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
        verify(awsS3Adapter).deleteObject("videos/key.mp4");
    }

    @Test
    void parts에_null_요소가_있으면_InvalidVideoFileException이_발생한다() {
        List<ApplyPartInput> parts = new java.util.ArrayList<>();
        parts.add(new ApplyPartInput(1, "etag1"));
        parts.add(null);
        ApplyCompleteRequest req = new ApplyCompleteRequest("videos/key.mp4", "upload-id", "x.mp4", parts);

        assertThatThrownBy(() -> applyService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void parts의_etag가_비어있으면_InvalidVideoFileException이_발생한다() {
        ApplyCompleteRequest req = new ApplyCompleteRequest("videos/key.mp4", "upload-id", "x.mp4",
                List.of(new ApplyPartInput(1, "  ")));

        assertThatThrownBy(() -> applyService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void parts에_중복된_partNumber가_있으면_InvalidVideoFileException이_발생한다() {
        ApplyCompleteRequest req = new ApplyCompleteRequest("videos/key.mp4", "upload-id", "x.mp4",
                List.of(new ApplyPartInput(1, "etag1"), new ApplyPartInput(1, "etag2")));

        assertThatThrownBy(() -> applyService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 파일명이_255자를_초과하면_잘라서_저장한다() {
        ApplyCompleteRequest req = request("videos/key.mp4", "upload-id", "가".repeat(300) + ".mp4");
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class))).willAnswer(i -> i.getArgument(0));

        applyService.execute(req);

        verify(applyRepository).save(argThat(a ->
                a.getOriginalFilename().length() == 255 && a.getOriginalFilename().endsWith(".mp4")));
    }

    @Test
    void mp4가_아닌_확장자는_mp4로_변환해_저장한다() {
        ApplyCompleteRequest req = request("videos/key.mp4", "upload-id", "공연영상.mov");
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class))).willAnswer(i -> i.getArgument(0));

        applyService.execute(req);

        verify(applyRepository).save(argThat(a -> a.getOriginalFilename().equals("공연영상.mp4")));
    }

    @Test
    void 확장자가_없으면_mp4를_붙여_저장한다() {
        ApplyCompleteRequest req = request("videos/key.mp4", "upload-id", "공연영상");
        given(awsS3Adapter.readObjectHead(anyString(), anyInt())).willReturn(VALID_MP4_HEADER);
        given(applyRepository.save(any(ApplyEntity.class))).willAnswer(i -> i.getArgument(0));

        applyService.execute(req);

        verify(applyRepository).save(argThat(a -> a.getOriginalFilename().equals("공연영상.mp4")));
    }
}
