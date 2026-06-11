package team.startup.gwangjutalentfestival.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.UploadTeamVideoResponse;
import team.startup.gwangjutalentfestival.domain.team.service.UploadTeamVideoService;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

@Service
@RequiredArgsConstructor
public class UploadTeamVideoServiceImpl implements UploadTeamVideoService {

    private final AwsS3Adapter awsS3Adapter;

    @Override
    public UploadTeamVideoResponse execute(MultipartFile file) {
        if (file.isEmpty() || !"video/mp4".equals(file.getContentType())) {
            throw new InvalidVideoFileException();
        }
        String url = awsS3Adapter.uploadVideo(file);
        return new UploadTeamVideoResponse(url);
    }
}
