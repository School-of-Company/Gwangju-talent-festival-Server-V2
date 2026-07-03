package team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ExportDatasetRequest(
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end,
        @Min(60) @Max(3600) Integer stepSeconds,
        String domain,
        String metricName
) {}
