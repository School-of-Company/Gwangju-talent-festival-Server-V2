package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.UploadTeamVideoResponse;
import team.startup.gwangjutalentfestival.domain.team.service.UploadTeamVideoService;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class UploadTeamVideoServiceImpl implements UploadTeamVideoService {

    private final AwsS3Adapter awsS3Adapter;

    @Override
    public UploadTeamVideoResponse execute(MultipartFile file) {
        if (file == null || file.isEmpty() || !isMp4File(file)) {
            throw new InvalidVideoFileException();
        }
        String url = awsS3Adapter.uploadVideo(file);
        return new UploadTeamVideoResponse(url);
    }

    private boolean isMp4File(MultipartFile file) {
        try {
            byte[] header = new byte[12];
            try (InputStream is = file.getInputStream()) {
                if (is.read(header) < 12) return false;
            }
            // ftyp box: offset 4~7 == 0x66 0x74 0x79 0x70
            return header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70;
        } catch (IOException e) {
            return false;
        }
    }
}
