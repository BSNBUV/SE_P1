package com.astra.command.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.command.common.MissionStatus;
import org.junit.jupiter.api.Test;

class SimulatedTelemetryParserTest {
    @Test
    void parsesSimulatorFrame() {
        CommonTelemetry telemetry = new SimulatedTelemetryParser()
                .parse("SITL,7,1700000000000,12.9,77.5,42.0,88.5,EXECUTING:33.0");

        assertThat(telemetry.missionId()).isEqualTo(7);
        assertThat(telemetry.missionStatus()).isEqualTo(MissionStatus.EXECUTING);
        assertThat(telemetry.progress()).isEqualTo(33.0);
    }
}
