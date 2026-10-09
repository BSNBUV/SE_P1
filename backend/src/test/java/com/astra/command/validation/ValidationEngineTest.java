package com.astra.command.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.command.common.ValidationOutcome;
import com.astra.command.mission.Mission;
import com.astra.command.mission.Waypoint;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationEngineTest {
    private final ValidationEngine engine = new ValidationEngine(props());

    @Test
    void validatesSafeRoute() {
        Mission mission = mission(List.of(
                new Waypoint(1, 12.97160, 77.59460, 40),
                new Waypoint(2, 12.97300, 77.59610, 45),
                new Waypoint(3, 12.97420, 77.59380, 42)));

        ValidationResult result = engine.validate(mission);

        assertThat(result.overall()).isEqualTo(ValidationOutcome.PASS);
        assertThat(result.distanceKm()).isGreaterThan(0);
        assertThat(result.summary()).contains("BATTERY FEASIBILITY");
    }

    @Test
    void rejectsDuplicateConsecutiveWaypoint() {
        Mission mission = mission(List.of(
                new Waypoint(1, 12.97160, 77.59460, 40),
                new Waypoint(2, 12.97160, 77.59460, 40)));

        assertThat(engine.validate(mission).overall()).isEqualTo(ValidationOutcome.REJECT);
    }

    private Mission mission(List<Waypoint> waypoints) {
        Mission mission = new Mission("T", "T", "tester", "Astra-SITL-01");
        mission.replaceWaypoints(waypoints);
        return mission;
    }

    private ValidationProperties props() {
        ValidationProperties p = new ValidationProperties();
        p.setMinWaypoints(2);
        p.setMinAltitudeMeters(5);
        p.setMaxAltitudeMeters(120);
        p.setBaseBatteryCost(4);
        p.setCostPerKm(7.5);
        p.setAltitudeCostFactor(0.025);
        p.setWarningReserve(25);
        p.setRejectReserve(10);
        return p;
    }
}
