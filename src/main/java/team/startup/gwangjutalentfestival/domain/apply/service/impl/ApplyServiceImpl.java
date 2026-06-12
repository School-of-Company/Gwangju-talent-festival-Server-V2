package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.domain.apply.service.ApplyService;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private static final int MP4_HEADER_LENGTH = 12;
    private static final String DEFAULT_FILENAME = "video.mp4";
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String VIDEO_KEY_PATTERN = "^videos/[a-zA-Z0-9_-]+\\.mp4$";

    private final AwsS3Adapter awsS3Adapter;
    private final ApplyRepository applyRepository;

    @Override
    @Transactional
    public ApplyResponse execute(ApplyRequest request) {
        if (request == null || request.key() == null || !request.key().matches(VIDEO_KEY_PATTERN)) {
            throw new InvalidVideoFileException();
        }
        validateUploadedMp4(request.key());

        ApplyEntity apply = applyRepository.save(
                ApplyEntity.builder()
                        .videoKey(request.key())
                        .originalFilename(resolveFilename(request.filename()))
                        .build()
        );
        return new ApplyResponse(apply.getId());
    }

    private String resolveFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return DEFAULT_FILENAME;
        }
        return filename.length() > MAX_FILENAME_LENGTH
                ? filename.substring(0, MAX_FILENAME_LENGTH)
                : filename;
    }

    private void validateUploadedMp4(String key) {
        byte[] header = awsS3Adapter.readObjectHead(key, MP4_HEADER_LENGTH);
        if (header.length < MP4_HEADER_LENGTH || !isMp4Header(header)) {
            throw new InvalidVideoFileException();
        }
    }

    private boolean isMp4Header(byte[] header) {
        // ftyp box: offset 4~7 == 0x66 0x74 0x79 0x70
        return header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70;
    }
}
