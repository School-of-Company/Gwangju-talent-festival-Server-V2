package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.domain.apply.service.ApplyService;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private static final int MP4_HEADER_LENGTH = 12;
    private static final String DEFAULT_FILENAME = "video.mp4";

    private final AwsS3Adapter awsS3Adapter;
    private final ApplyRepository applyRepository;

    @Override
    @Transactional
    public ApplyResponse execute(MultipartFile file) {
        if (file == null || file.isEmpty() || !isMp4File(file)) {
            throw new InvalidVideoFileException();
        }
        String key = awsS3Adapter.uploadVideo(file);
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : DEFAULT_FILENAME;
        ApplyEntity apply = applyRepository.save(
                ApplyEntity.builder()
                        .videoKey(key)
                        .originalFilename(filename)
                        .build()
        );
        String url = awsS3Adapter.generateVideoDownloadUrl(key, filename);
        return new ApplyResponse(apply.getId(), url);
    }

    private boolean isMp4File(MultipartFile file) {
        try {
            byte[] header = new byte[MP4_HEADER_LENGTH];
            try (InputStream is = file.getInputStream()) {
                if (is.readNBytes(header, 0, MP4_HEADER_LENGTH) < MP4_HEADER_LENGTH) return false;
            }
            // ftyp box: offset 4~7 == 0x66 0x74 0x79 0x70
            return header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70;
        } catch (IOException e) {
            return false;
        }
    }
}
