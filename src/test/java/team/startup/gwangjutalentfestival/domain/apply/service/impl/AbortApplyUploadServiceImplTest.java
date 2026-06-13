package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyAbortRequest;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbortApplyUploadServiceImplTest {

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @InjectMocks
    private AbortApplyUploadServiceImpl abortApplyUploadService;

    @Test
    void 유효한_요청이면_멀티파트_업로드를_취소한다() {
        ApplyAbortRequest req = new ApplyAbortRequest("videos/key.mp4", "upload-id");

        abortApplyUploadService.execute(req);

        verify(awsS3Adapter).abortMultipartUpload("videos/key.mp4", "upload-id");
    }

    @Test
    void key가_규격을_벗어나면_InvalidVideoFileException이_발생한다() {
        ApplyAbortRequest req = new ApplyAbortRequest("uploads/evil.mp4", "upload-id");

        assertThatThrownBy(() -> abortApplyUploadService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }
}
