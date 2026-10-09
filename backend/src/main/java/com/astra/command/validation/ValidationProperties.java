package com.astra.command.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "astra.validation")
public class ValidationProperties {
    private int minWaypoints;
    private double maxAltitudeMeters;
    private double minAltitudeMeters;
    private double baseBatteryCost;
    private double costPerKm;
    private double altitudeCostFactor;
    private double warningReserve;
    private double rejectReserve;

    public int getMinWaypoints() { return minWaypoints; }
    public void setMinWaypoints(int minWaypoints) { this.minWaypoints = minWaypoints; }
    public double getMaxAltitudeMeters() { return maxAltitudeMeters; }
    public void setMaxAltitudeMeters(double maxAltitudeMeters) { this.maxAltitudeMeters = maxAltitudeMeters; }
    public double getMinAltitudeMeters() { return minAltitudeMeters; }
    public void setMinAltitudeMeters(double minAltitudeMeters) { this.minAltitudeMeters = minAltitudeMeters; }
    public double getBaseBatteryCost() { return baseBatteryCost; }
    public void setBaseBatteryCost(double baseBatteryCost) { this.baseBatteryCost = baseBatteryCost; }
    public double getCostPerKm() { return costPerKm; }
    public void setCostPerKm(double costPerKm) { this.costPerKm = costPerKm; }
    public double getAltitudeCostFactor() { return altitudeCostFactor; }
    public void setAltitudeCostFactor(double altitudeCostFactor) { this.altitudeCostFactor = altitudeCostFactor; }
    public double getWarningReserve() { return warningReserve; }
    public void setWarningReserve(double warningReserve) { this.warningReserve = warningReserve; }
    public double getRejectReserve() { return rejectReserve; }
    public void setRejectReserve(double rejectReserve) { this.rejectReserve = rejectReserve; }
}
