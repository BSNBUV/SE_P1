package com.astra.command.mission;

import com.astra.command.adapter.CommonMission;
import com.astra.command.adapter.MissionAdapter;
import com.astra.command.alert.Alert;
import com.astra.command.alert.AlertRepository;
import com.astra.command.common.MissionStatus;
import com.astra.command.log.MissionLog;
import com.astra.command.log.MissionLogRepository;
import com.astra.command.mission.MissionDtos.MissionRequest;
import com.astra.command.mission.MissionDtos.MissionResponse;
import com.astra.command.telemetry.Telemetry;
import com.astra.command.telemetry.TelemetryRepository;
import com.astra.command.validation.ValidationJobService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class MissionController {
    private final MissionRepository missions;
    private final ValidationJobService validationJobs;
    private final MissionAdapter adapter;
    private final TelemetryRepository telemetry;
    private final AlertRepository alerts;
    private final MissionLogRepository logs;

    public MissionController(MissionRepository missions, ValidationJobService validationJobs, MissionAdapter adapter,
                             TelemetryRepository telemetry, AlertRepository alerts, MissionLogRepository logs) {
        this.missions = missions;
        this.validationJobs = validationJobs;
        this.adapter = adapter;
        this.telemetry = telemetry;
        this.alerts = alerts;
        this.logs = logs;
    }

    @GetMapping("/missions")
    public List<MissionResponse> list() {
        return missions.findAll().stream()
                .sorted(Comparator.comparing(Mission::getCreatedAt).reversed())
                .map(MissionResponse::from)
                .toList();
    }

    @PostMapping("/missions")
    public MissionResponse create(@Valid @RequestBody MissionRequest request, Principal principal) {
        Mission mission = new Mission(request.name(), request.description(), principal.getName(), request.vehicleId());
        mission.replaceWaypoints(request.waypoints().stream()
                .map(w -> new Waypoint(w.sequence(), w.latitude(), w.longitude(), w.altitude()))
                .toList());
        Mission saved = missions.save(mission);
        logs.save(new MissionLog(saved.getId(), "MISSION_CREATED", "Mission created by " + principal.getName(), "{}"));
        return MissionResponse.from(saved);
    }

    @GetMapping("/missions/{id}")
    public MissionResponse get(@PathVariable Long id) {
        return MissionResponse.from(mission(id));
    }

    @PostMapping("/missions/{id}/validate")
    public MissionResponse validate(@PathVariable Long id) {
        return MissionResponse.from(validationJobs.submit(id));
    }

    @GetMapping("/missions/{id}/validation")
    public MissionResponse validation(@PathVariable Long id) {
        return get(id);
    }

    @PostMapping("/missions/{id}/execute")
    public MissionResponse execute(@PathVariable Long id) {
        Mission mission = mission(id);
        if (mission.getStatus() != MissionStatus.VALIDATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mission must be VALIDATED before execution");
        }
        adapter.execute(CommonMission.from(mission));
        return MissionResponse.from(mission(id));
    }

    @PostMapping("/missions/{id}/pause")
    public MissionResponse pause(@PathVariable Long id) {
        adapter.pause(id);
        return get(id);
    }

    @PostMapping("/missions/{id}/resume")
    public MissionResponse resume(@PathVariable Long id) {
        adapter.resume(id);
        return get(id);
    }

    @PostMapping("/missions/{id}/emergency-stop")
    public MissionResponse emergencyStop(@PathVariable Long id) {
        adapter.emergencyStop(id);
        return get(id);
    }

    @PostMapping("/missions/{id}/simulate-low-battery")
    public MissionResponse lowBattery(@PathVariable Long id) {
        adapter.simulateLowBattery(id);
        return get(id);
    }

    @PostMapping("/missions/{id}/simulate-connection-loss")
    public MissionResponse connectionLoss(@PathVariable Long id) {
        adapter.simulateConnectionLoss(id);
        return get(id);
    }

    @PostMapping("/missions/{id}/reset")
    public MissionResponse reset(@PathVariable Long id) {
        adapter.reset(id);
        return get(id);
    }

    @GetMapping("/missions/{id}/telemetry")
    public List<Telemetry> telemetry(@PathVariable Long id) {
        return telemetry.findByMissionIdOrderByTimestampAsc(id);
    }

    @GetMapping("/missions/{id}/alerts")
    public List<Alert> alerts(@PathVariable Long id) {
        return alerts.findByMissionIdOrderByTimestampAsc(id);
    }

    @GetMapping("/missions/{id}/logs")
    public List<MissionLog> logs(@PathVariable Long id) {
        return logs.findByMissionIdOrderByTimestampAsc(id);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<Mission> all = missions.findAll();
        Mission active = all.stream().filter(m -> m.getStatus() == MissionStatus.EXECUTING || m.getStatus() == MissionStatus.PAUSED)
                .findFirst()
                .orElse(all.stream().max(Comparator.comparing(Mission::getUpdatedAt)).orElse(null));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mode", "SIMULATION / SITL MODE");
        response.put("systemStatus", "OPERATIONAL");
        response.put("simulatorConnection", active == null ? "IDLE" : adapter.connectionStatus(active.getId()));
        response.put("activeMission", active == null ? null : MissionResponse.from(active));
        response.put("latestTelemetry", active == null ? null : telemetry.findTopByMissionIdOrderByTimestampDesc(active.getId()).orElse(null));
        response.put("recentAlerts", alerts.findTop20ByOrderByTimestampDesc());
        response.put("recentLogs", logs.findTop20ByOrderByTimestampDesc());
        return response;
    }

    @GetMapping("/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MissionLog> audit() {
        return logs.findTop20ByOrderByTimestampDesc();
    }

    private Mission mission(Long id) {
        return missions.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission not found"));
    }
}
