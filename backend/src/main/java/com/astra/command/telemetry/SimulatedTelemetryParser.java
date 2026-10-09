package com.astra.command.telemetry;

import com.astra.command.common.MissionStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SimulatedTelemetryParser implements TelemetryParser {
    @Override
    public CommonTelemetry parse(String frame) {
        String[] p = frame.split(",");
        if (p.length != 8 || !"SITL".equals(p[0])) {
            throw new IllegalArgumentException("Malformed simulator telemetry frame");
        }
        return new CommonTelemetry(
                Long.parseLong(p[1]),
                Instant.ofEpochMilli(Long.parseLong(p[2])),
                Double.parseDouble(p[3]),
                Double.parseDouble(p[4]),
                Double.parseDouble(p[5]),
                Double.parseDouble(p[6]),
                MissionStatus.valueOf(p[7].split(":")[0]),
                Double.parseDouble(p[7].split(":")[1]));
    }
}
