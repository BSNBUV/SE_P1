package com.astra.command.validation;

import com.astra.command.common.GeoMath;
import com.astra.command.common.ValidationOutcome;
import com.astra.command.mission.Mission;
import com.astra.command.mission.Waypoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationEngine {
    private final ValidationProperties props;

    public ValidationEngine(ValidationProperties props) {
        this.props = props;
    }

    public ValidationResult validate(Mission mission) {
        List<Waypoint> waypoints = mission.getWaypoints().stream()
                .sorted(Comparator.comparingInt(Waypoint::getSequence))
                .toList();
        List<ValidationResult.RuleResult> rules = new ArrayList<>();
        rules.add(waypointCheck(waypoints));
        double distance = routeDistance(waypoints);
        rules.add(routeCheck(waypoints, distance));
        rules.add(altitudeCheck(waypoints));
        BatteryEstimate battery = batteryEstimate(waypoints, distance);
        rules.add(battery.rule());

        ValidationOutcome overall = classify(rules);
        String summary = rules.stream()
                .map(r -> r.name() + " " + r.outcome() + ": " + r.reason())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No validation rules ran");
        return new ValidationResult(overall, rules, distance, battery.used(), battery.reserve(), summary);
    }

    public double routeDistance(List<Waypoint> waypoints) {
        double total = 0;
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint a = waypoints.get(i - 1);
            Waypoint b = waypoints.get(i);
            total += GeoMath.haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
        }
        return total;
    }

    private ValidationResult.RuleResult waypointCheck(List<Waypoint> waypoints) {
        if (waypoints.size() < props.getMinWaypoints()) {
            return rule("WAYPOINT CHECK", ValidationOutcome.REJECT, "At least " + props.getMinWaypoints() + " waypoints are required.");
        }
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint w = waypoints.get(i);
            if (w.getSequence() != i + 1) {
                return rule("WAYPOINT CHECK", ValidationOutcome.REJECT, "Waypoint sequence must be contiguous from 1.");
            }
            if (!Double.isFinite(w.getLatitude()) || w.getLatitude() < -90 || w.getLatitude() > 90
                    || !Double.isFinite(w.getLongitude()) || w.getLongitude() < -180 || w.getLongitude() > 180) {
                return rule("WAYPOINT CHECK", ValidationOutcome.REJECT, "Waypoint " + w.getSequence() + " has malformed coordinates.");
            }
        }
        return rule("WAYPOINT CHECK", ValidationOutcome.PASS, "Waypoint count, coordinate ranges and order are valid.");
    }

    private ValidationResult.RuleResult routeCheck(List<Waypoint> waypoints, double distance) {
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint a = waypoints.get(i - 1);
            Waypoint b = waypoints.get(i);
            if (GeoMath.haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude()) < 0.005) {
                return rule("ROUTE CONSISTENCY", ValidationOutcome.REJECT, "Consecutive waypoints " + a.getSequence() + " and " + b.getSequence() + " are duplicates or too close.");
            }
        }
        if (distance <= 0 || !Double.isFinite(distance)) {
            return rule("ROUTE CONSISTENCY", ValidationOutcome.REJECT, "Route distance is not geometrically valid.");
        }
        return rule("ROUTE CONSISTENCY", ValidationOutcome.PASS, "Total route distance is " + round(distance) + " km.");
    }

    private ValidationResult.RuleResult altitudeCheck(List<Waypoint> waypoints) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getAltitude() < props.getMinAltitudeMeters() || waypoint.getAltitude() > props.getMaxAltitudeMeters()) {
                return rule("ALTITUDE SAFETY", ValidationOutcome.REJECT,
                        "Waypoint " + waypoint.getSequence() + " altitude " + waypoint.getAltitude()
                                + "m is outside configured " + props.getMinAltitudeMeters() + "-"
                                + props.getMaxAltitudeMeters() + "m bounds.");
            }
        }
        return rule("ALTITUDE SAFETY", ValidationOutcome.PASS, "All waypoint altitudes are within configured bounds.");
    }

    private BatteryEstimate batteryEstimate(List<Waypoint> waypoints, double distance) {
        double maxAltitude = waypoints.stream().mapToDouble(Waypoint::getAltitude).max().orElse(0);
        double used = props.getBaseBatteryCost() + distance * props.getCostPerKm() + maxAltitude * props.getAltitudeCostFactor();
        double reserve = Math.max(0, 100 - used);
        ValidationOutcome outcome = reserve < props.getRejectReserve()
                ? ValidationOutcome.REJECT
                : reserve < props.getWarningReserve() ? ValidationOutcome.WARNING : ValidationOutcome.PASS;
        String reason = "battery_used = base_cost(" + props.getBaseBatteryCost() + ") + distance_km("
                + round(distance) + ") * cost_per_km(" + props.getCostPerKm() + ") + altitude_factor("
                + round(maxAltitude * props.getAltitudeCostFactor()) + "); reserve = " + round(reserve) + "%.";
        return new BatteryEstimate(used, reserve, rule("BATTERY FEASIBILITY", outcome, reason));
    }

    private ValidationOutcome classify(List<ValidationResult.RuleResult> rules) {
        if (rules.stream().anyMatch(r -> r.outcome() == ValidationOutcome.REJECT)) {
            return ValidationOutcome.REJECT;
        }
        if (rules.stream().anyMatch(r -> r.outcome() == ValidationOutcome.WARNING)) {
            return ValidationOutcome.WARNING;
        }
        return ValidationOutcome.PASS;
    }

    private ValidationResult.RuleResult rule(String name, ValidationOutcome outcome, String reason) {
        return new ValidationResult.RuleResult(name, outcome, reason);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record BatteryEstimate(double used, double reserve, ValidationResult.RuleResult rule) {
    }
}
