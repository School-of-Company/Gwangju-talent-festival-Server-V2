package team.startup.gwangjutalentfestival.domain.team.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.UploadTeamVideoResponse;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UploadTeamVideoServiceImplTest {

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @InjectMocks
    private UploadTeamVideoServiceImpl uploadTeamVideoService;

    private byte[] validMp4Header;

    @BeforeEach
    void setUp() {
        validMp4Header = new byte[]{
                0x00, 0x00, 0x00, 0x1C,
                0x66, 0x74, 0x79, 0x70,  // ftyp
                0x69, 0x73, 0x6F, 0x6D   // isom
        };
    }

    @Test
    void 유효한_MP4_파일이면_업로드_성공_후_URL을_반환한다() {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", validMp4Header);
        given(awsS3Adapter.uploadVideo(any())).willReturn("https://s3.example.com/videos/video.mp4");

        UploadTeamVideoResponse response = uploadTeamVideoService.execute(file);

        assertThat(response.videoUrl()).isEqualTo("https://s3.example.com/videos/video.mp4");
    }

    @Test
    void 빈_파일이면_InvalidVideoFileException이_발생한다() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.mp4", "video/mp4", new byte[0]);

        assertThatThrownBy(() -> uploadTeamVideoService.execute(emptyFile))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void MP4_시그니처가_아닌_파일이면_InvalidVideoFileException이_발생한다() {
        byte[] fakeContent = new byte[]{
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
        MockMultipartFile fakeFile = new MockMultipartFile("file", "fake.mp4", "video/mp4", fakeContent);

        assertThatThrownBy(() -> uploadTeamVideoService.execute(fakeFile))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void 파일_크기가_12바이트_미만이면_InvalidVideoFileException이_발생한다() {
        MockMultipartFile shortFile = new MockMultipartFile("file", "short.mp4", "video/mp4", new byte[]{0x00, 0x01});

        assertThatThrownBy(() -> uploadTeamVideoService.execute(shortFile))
                .isInstanceOf(InvalidVideoFileException.class);
    }
}
