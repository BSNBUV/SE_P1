package com.astra.command.telemetry;

public interface TelemetryParser {
    CommonTelemetry parse(String simulatorFrame);
}
