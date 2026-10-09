package com.astra.command.report;

import com.astra.command.alert.AlertRepository;
import com.astra.command.mission.Mission;
import com.astra.command.mission.MissionRepository;
import com.astra.command.telemetry.TelemetryRepository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private final MissionRepository missions;
    private final TelemetryRepository telemetry;
    private final AlertRepository alerts;

    public MetricsController(MissionRepository missions, TelemetryRepository telemetry, AlertRepository alerts) {
        this.missions = missions;
        this.telemetry = telemetry;
        this.alerts = alerts;
    }

    @GetMapping
    Map<String, Object> metrics() {
        List<Long> latencies = missions.findAll().stream()
                .filter(m -> m.getValidationRequestedAt() != null && m.getValidationCompletedAt() != null)
                .map(m -> Duration.between(m.getValidationRequestedAt(), m.getValidationCompletedAt()).toMillis())
                .sorted()
                .toList();
        Long median = latencies.isEmpty() ? null : latencies.get(latencies.size() / 2);
        long packets = telemetry.count();
        return Map.of(
                "medianMissionValidationLatencyMs", median == null ? "not measured" : median,
                "telemetryPacketsReceived", packets,
                "telemetryPacketsParsed", packets,
                "parsingSuccessRate", packets == 0 ? "not measured" : 1.0,
                "alertCount", alerts.count(),
                "jobStatus", "in-process worker with Redis-backed queue record");
    }
}
