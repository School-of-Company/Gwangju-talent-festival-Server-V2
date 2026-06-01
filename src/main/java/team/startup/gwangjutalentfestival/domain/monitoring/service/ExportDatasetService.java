package team.startup.gwangjutalentfestival.domain.monitoring.service;

import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.ExportDatasetRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.ExportDatasetResponse;

public interface ExportDatasetService {
    ExportDatasetResponse execute(ExportDatasetRequest request);
}