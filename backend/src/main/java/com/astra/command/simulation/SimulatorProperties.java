package com.astra.command.simulation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "astra.simulation")
public class SimulatorProperties {
    private long tickMs;
    private double lowBatteryThreshold;
    private long telemetryTimeoutMs;

    public long getTickMs() { return tickMs; }
    public void setTickMs(long tickMs) { this.tickMs = tickMs; }
    public double getLowBatteryThreshold() { return lowBatteryThreshold; }
    public void setLowBatteryThreshold(double lowBatteryThreshold) { this.lowBatteryThreshold = lowBatteryThreshold; }
    public long getTelemetryTimeoutMs() { return telemetryTimeoutMs; }
    public void setTelemetryTimeoutMs(long telemetryTimeoutMs) { this.telemetryTimeoutMs = telemetryTimeoutMs; }
}
