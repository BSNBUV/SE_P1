package com.astra.command.simulation;

import com.astra.command.adapter.CommonMission;
import com.astra.command.alert.Alert;
import com.astra.command.alert.AlertRepository;
import com.astra.command.common.AlertSeverity;
import com.astra.command.common.MissionStatus;
import com.astra.command.log.MissionLog;
import com.astra.command.log.MissionLogRepository;
import com.astra.command.mission.Mission;
import com.astra.command.mission.MissionRepository;
import com.astra.command.telemetry.CommonTelemetry;
import com.astra.command.telemetry.SimulatedTelemetryParser;
import com.astra.command.telemetry.Telemetry;
import com.astra.command.telemetry.TelemetryRepository;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(SimulatorProperties.class)
public class SimulatorService {
    private final MissionRepository missions;
    private final TelemetryRepository telemetry;
    private final AlertRepository alerts;
    private final MissionLogRepository logs;
    private final SimulatedTelemetryParser parser;
    private final SimulatorProperties props;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public SimulatorService(MissionRepository missions, TelemetryRepository telemetry, AlertRepository alerts,
                            MissionLogRepository logs, SimulatedTelemetryParser parser, SimulatorProperties props) {
        this.missions = missions;
        this.telemetry = telemetry;
        this.alerts = alerts;
        this.logs = logs;
        this.parser = parser;
        this.props = props;
    }

    public void start(CommonMission mission) {
        Mission entity = missions.findById(mission.id()).orElseThrow();
        if (entity.getStatus() != MissionStatus.VALIDATED && entity.getStatus() != MissionStatus.PAUSED) {
            throw new IllegalStateException("Mission must be VALIDATED before execution");
        }
        entity.setStatus(MissionStatus.EXECUTING);
        entity.setExecutionStartedAt(Instant.now());
        missions.save(entity);
        logs.save(new MissionLog(mission.id(), "ADAPTER_EXECUTE", "SimulatedSITLAdapter accepted common mission", "{\"adapter\":\"SimulatedSITLAdapter\"}"));
        Session session = new Session(mission);
        sessions.put(mission.id(), session);
        session.future = scheduler.scheduleAtFixedRate(() -> tick(session), 0, props.getTickMs(), TimeUnit.MILLISECONDS);
    }

    public void pause(Long missionId) {
        Session s = sessions.get(missionId);
        if (s != null) {
            s.paused = true;
            setStatus(missionId, MissionStatus.PAUSED, "MISSION_PAUSED", "Simulation paused");
        }
    }

    public void resume(Long missionId) {
        Session s = sessions.get(missionId);
        if (s != null) {
            s.paused = false;
            setStatus(missionId, MissionStatus.EXECUTING, "MISSION_RESUMED", "Simulation resumed");
        }
    }

    public void emergencyStop(Long missionId) {
        Session s = sessions.remove(missionId);
        if (s != null && s.future != null) {
            s.future.cancel(false);
        }
        setStatus(missionId, MissionStatus.STOPPED, "EMERGENCY_STOP", "Emergency stop requested by operator");
        alerts.save(new Alert(missionId, AlertSeverity.CRITICAL, "EMERGENCY_STOP", "Mission stopped by emergency control."));
    }

    public void simulateLowBattery(Long missionId) {
        Session s = sessions.get(missionId);
        if (s != null) {
            s.forceLowBattery = true;
            logs.save(new MissionLog(missionId, "SIM_LOW_BATTERY", "Low battery scenario injected", "{}"));
        }
    }

    public void simulateConnectionLoss(Long missionId) {
        Session s = sessions.get(missionId);
        if (s != null) {
            s.connectionLost = true;
            alerts.save(new Alert(missionId, AlertSeverity.CRITICAL, "CONNECTION_LOSS", "Simulator telemetry connection lost."));
            logs.save(new MissionLog(missionId, "CONNECTION_LOSS", "Telemetry stream stopped by failure scenario", "{}"));
        }
    }

    public void reset(Long missionId) {
        Session s = sessions.remove(missionId);
        if (s != null && s.future != null) {
            s.future.cancel(false);
        }
        Mission mission = missions.findById(missionId).orElseThrow();
        mission.setStatus(mission.getValidationResult() == null ? MissionStatus.CREATED : MissionStatus.VALIDATED);
        missions.save(mission);
        logs.save(new MissionLog(missionId, "SIM_RESET", "Simulation session reset", "{}"));
    }

    public String connectionStatus(Long missionId) {
        Session s = sessions.get(missionId);
        if (s == null) {
            return "IDLE";
        }
        if (s.connectionLost) {
            return "LOST";
        }
        if (s.paused) {
            return "PAUSED";
        }
        return "CONNECTED";
    }

    private void tick(Session s) {
        if (s.paused || s.connectionLost) {
            return;
        }
        try {
            int last = s.mission.waypoints().size() - 1;
            if (last < 1) {
                complete(s);
                return;
            }
            double segmentProgress = (s.stepInSegment % 10) / 10.0;
            var a = s.mission.waypoints().get(s.segment);
            var b = s.mission.waypoints().get(Math.min(s.segment + 1, last));
            double lat = interpolate(a.latitude(), b.latitude(), segmentProgress);
            double lon = interpolate(a.longitude(), b.longitude(), segmentProgress);
            double alt = interpolate(a.altitude(), b.altitude(), segmentProgress);
            s.battery = s.forceLowBattery ? Math.min(s.battery, 14.0) : Math.max(0, s.battery - 0.55);
            double progress = ((s.segment + segmentProgress) / last) * 100.0;
            String frame = "SITL," + s.mission.id() + "," + Instant.now().toEpochMilli() + "," + lat + "," + lon + "," + alt + "," + s.battery + "," + MissionStatus.EXECUTING + ":" + progress;
            CommonTelemetry parsed = parser.parse(frame);
            telemetry.save(new Telemetry(parsed.missionId(), parsed.timestamp(), parsed.latitude(), parsed.longitude(),
                    parsed.altitude(), parsed.battery(), parsed.missionStatus(), parsed.progress()));
            s.packetsParsed++;
            if (parsed.battery() < props.getLowBatteryThreshold() && !s.lowBatteryAlerted) {
                s.lowBatteryAlerted = true;
                alerts.save(new Alert(s.mission.id(), AlertSeverity.CRITICAL, "LOW_BATTERY", "Battery below configured 15% safety threshold."));
                logs.save(new MissionLog(s.mission.id(), "LOW_BATTERY", "Telemetry parser detected battery " + parsed.battery() + "%", "{}"));
            }
            s.stepInSegment++;
            if (s.stepInSegment > 10) {
                s.segment++;
                s.stepInSegment = 0;
                logs.save(new MissionLog(s.mission.id(), "WAYPOINT_REACHED", "Reached waypoint " + (s.segment + 1), "{}"));
            }
            if (s.segment >= last) {
                complete(s);
            }
        } catch (Exception ex) {
            alerts.save(new Alert(s.mission.id(), AlertSeverity.CRITICAL, "SIMULATOR_ERROR", ex.getMessage()));
            setStatus(s.mission.id(), MissionStatus.FAILURE, "SIMULATOR_ERROR", ex.getMessage());
        }
    }

    private void complete(Session s) {
        if (s.future != null) {
            s.future.cancel(false);
        }
        sessions.remove(s.mission.id());
        Mission mission = missions.findById(s.mission.id()).orElseThrow();
        mission.setStatus(MissionStatus.COMPLETED);
        mission.setExecutionCompletedAt(Instant.now());
        missions.save(mission);
        alerts.save(new Alert(s.mission.id(), AlertSeverity.INFO, "MISSION_COMPLETED", "Simulated mission completed."));
        logs.save(new MissionLog(s.mission.id(), "MISSION_COMPLETED", "Mission reached final waypoint", "{\"packetsParsed\":" + s.packetsParsed + "}"));
    }

    private void setStatus(Long missionId, MissionStatus status, String event, String message) {
        Mission mission = missions.findById(missionId).orElseThrow();
        mission.setStatus(status);
        if (status == MissionStatus.STOPPED || status == MissionStatus.FAILURE) {
            mission.setExecutionCompletedAt(Instant.now());
        }
        missions.save(mission);
        logs.save(new MissionLog(missionId, event, message, "{}"));
    }

    private double interpolate(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static class Session {
        final CommonMission mission;
        volatile boolean paused;
        volatile boolean forceLowBattery;
        volatile boolean connectionLost;
        volatile boolean lowBatteryAlerted;
        int segment;
        int stepInSegment;
        double battery = 100;
        long packetsParsed;
        ScheduledFuture<?> future;

        Session(CommonMission mission) {
            this.mission = mission;
        }
    }
}
