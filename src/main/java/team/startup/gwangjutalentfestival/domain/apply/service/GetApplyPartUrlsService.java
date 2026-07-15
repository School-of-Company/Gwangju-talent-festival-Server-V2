package team.startup.gwangjutalentfestival.domain.apply.service;

import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyPartUrlsRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyPartUrlsResponse;

public interface GetApplyPartUrlsService {
    ApplyPartUrlsResponse execute(ApplyPartUrlsRequest request);
}
