package com.astra.command.report;

import com.astra.command.alert.AlertRepository;
import com.astra.command.log.MissionLogRepository;
import com.astra.command.mission.Mission;
import com.astra.command.mission.MissionRepository;
import com.astra.command.telemetry.TelemetryRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions/{id}/report")
public class ReportController {
    private final MissionRepository missions;
    private final TelemetryRepository telemetry;
    private final AlertRepository alerts;
    private final MissionLogRepository logs;

    public ReportController(MissionRepository missions, TelemetryRepository telemetry,
                            AlertRepository alerts, MissionLogRepository logs) {
        this.missions = missions;
        this.telemetry = telemetry;
        this.alerts = alerts;
        this.logs = logs;
    }

    @GetMapping
    public ResponseEntity<byte[]> report(@PathVariable Long id) {
        Mission mission = missions.findById(id).orElseThrow();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Astra Command Mission Report"));
        document.add(new Paragraph("Mission: " + mission.getName()));
        document.add(new Paragraph("Status: " + mission.getStatus()));
        document.add(new Paragraph("Creator: " + mission.getCreatedBy()));
        document.add(new Paragraph("Vehicle: " + mission.getVehicleId()));
        document.add(new Paragraph("Estimated distance: " + mission.getEstimatedDistance() + " km"));
        document.add(new Paragraph("Validation: " + mission.getValidationResult()));
        document.add(new Paragraph("Battery used/reserve: " + mission.getEstimatedBatteryUsage() + "% / " + mission.getEstimatedBatteryReserve() + "%"));
        document.add(new Paragraph("Waypoints: " + mission.getWaypoints().size()));
        document.add(new Paragraph("Telemetry packets: " + telemetry.countByMissionId(id)));
        document.add(new Paragraph("Alerts: " + alerts.countByMissionId(id)));
        document.add(new Paragraph("Validation summary:\n" + mission.getValidationSummary()));
        document.add(new Paragraph("Mission logs:"));
        logs.findByMissionIdOrderByTimestampAsc(id).forEach(log -> document.add(new Paragraph(log.getTimestamp() + " " + log.getEventType() + " - " + log.getMessage())));
        document.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=astra-mission-" + id + "-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }
}
