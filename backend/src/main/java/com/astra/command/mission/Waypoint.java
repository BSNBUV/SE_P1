package com.astra.command.mission;

import jakarta.persistence.*;

@Entity
public class Waypoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Mission mission;
    private int sequence;
    private double latitude;
    private double longitude;
    private double altitude;

    protected Waypoint() {
    }

    public Waypoint(int sequence, double latitude, double longitude, double altitude) {
        this.sequence = sequence;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    void attach(Mission mission) {
        this.mission = mission;
    }

    public Long getId() { return id; }
    public int getSequence() { return sequence; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
}
