package com.astra.command.telemetry;

import com.astra.command.common.MissionStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Telemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long missionId;
    private Instant timestamp = Instant.now();
    private double latitude;
    private double longitude;
    private double altitude;
    private double battery;
    @Enumerated(EnumType.STRING)
    private MissionStatus missionStatus;
    private double progress;

    protected Telemetry() {
    }

    public Telemetry(Long missionId, Instant timestamp, double latitude, double longitude, double altitude,
                     double battery, MissionStatus missionStatus, double progress) {
        this.missionId = missionId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.battery = battery;
        this.missionStatus = missionStatus;
        this.progress = progress;
    }

    public Long getId() { return id; }
    public Long getMissionId() { return missionId; }
    public Instant getTimestamp() { return timestamp; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
    public double getBattery() { return battery; }
    public MissionStatus getMissionStatus() { return missionStatus; }
    public double getProgress() { return progress; }
}
