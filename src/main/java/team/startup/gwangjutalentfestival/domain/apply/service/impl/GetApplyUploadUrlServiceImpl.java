package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyUploadUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyUploadUrlService;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetApplyUploadUrlServiceImpl implements GetApplyUploadUrlService {

    private static final String VIDEO_KEY_PREFIX = "videos/";
    private static final String VIDEO_KEY_SUFFIX = ".mp4";

    private final AwsS3Adapter awsS3Adapter;

    @Override
    public ApplyUploadUrlResponse execute() {
        String key = VIDEO_KEY_PREFIX + UUID.randomUUID() + VIDEO_KEY_SUFFIX;
        String uploadUrl = awsS3Adapter.generateUploadUrl(key);
        return new ApplyUploadUrlResponse(key, uploadUrl);
    }
}
