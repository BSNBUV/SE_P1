package com.astra.command.validation;

import com.astra.command.common.ValidationOutcome;
import java.util.List;

public record ValidationResult(ValidationOutcome overall, List<RuleResult> rules,
                               double distanceKm, double estimatedBatteryUsage,
                               double estimatedBatteryReserve, String summary) {
    public record RuleResult(String name, ValidationOutcome outcome, String reason) {
    }
}
