package team.startup.gwangjutalentfestival.domain.team.service;

import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.UploadTeamVideoResponse;

public interface UploadTeamVideoService {
    UploadTeamVideoResponse execute(MultipartFile file);
}
