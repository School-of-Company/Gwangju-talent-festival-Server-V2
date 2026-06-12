package team.startup.gwangjutalentfestival.domain.apply.service;

import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;

public interface ApplyService {
    ApplyResponse execute(MultipartFile file);
}
