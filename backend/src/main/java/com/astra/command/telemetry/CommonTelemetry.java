package com.astra.command.telemetry;

import com.astra.command.common.MissionStatus;
import java.time.Instant;

public record CommonTelemetry(Long missionId, Instant timestamp, double latitude, double longitude,
                              double altitude, double battery, MissionStatus missionStatus, double progress) {
}
