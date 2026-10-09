package com.astra.command.mission;

import com.astra.command.common.MissionStatus;
import com.astra.command.common.ValidationOutcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class MissionDtos {
    private MissionDtos() {
    }

    public record WaypointRequest(int sequence, double latitude, double longitude, double altitude) {
    }

    public record MissionRequest(@NotBlank String name, String description, String vehicleId,
                                 @NotEmpty @Valid List<WaypointRequest> waypoints) {
    }

    public record WaypointResponse(Long id, int sequence, double latitude, double longitude, double altitude) {
        static WaypointResponse from(Waypoint waypoint) {
            return new WaypointResponse(waypoint.getId(), waypoint.getSequence(), waypoint.getLatitude(), waypoint.getLongitude(), waypoint.getAltitude());
        }
    }

    public record MissionResponse(Long id, String name, String description, MissionStatus status, String createdBy,
                                  String vehicleId, Instant createdAt, Instant updatedAt,
                                  ValidationOutcome validationResult, String validationSummary,
                                  double estimatedDistance, double estimatedBatteryUsage,
                                  double estimatedBatteryReserve, Long validationLatencyMs,
                                  Instant executionStartedAt, Instant executionCompletedAt,
                                  List<WaypointResponse> waypoints) {
        public static MissionResponse from(Mission mission) {
            Long latency = null;
            if (mission.getValidationRequestedAt() != null && mission.getValidationCompletedAt() != null) {
                latency = Duration.between(mission.getValidationRequestedAt(), mission.getValidationCompletedAt()).toMillis();
            }
            return new MissionResponse(
                    mission.getId(), mission.getName(), mission.getDescription(), mission.getStatus(), mission.getCreatedBy(),
                    mission.getVehicleId(), mission.getCreatedAt(), mission.getUpdatedAt(), mission.getValidationResult(),
                    mission.getValidationSummary(), mission.getEstimatedDistance(), mission.getEstimatedBatteryUsage(),
                    mission.getEstimatedBatteryReserve(), latency, mission.getExecutionStartedAt(), mission.getExecutionCompletedAt(),
                    mission.getWaypoints().stream().map(WaypointResponse::from).toList());
        }
    }
}
