package com.astra.command.log;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class MissionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long missionId;
    private Instant timestamp = Instant.now();
    private String eventType;
    @Column(length = 2000)
    private String message;
    @Column(length = 2000)
    private String metadata;

    protected MissionLog() {
    }

    public MissionLog(Long missionId, String eventType, String message, String metadata) {
        this.missionId = missionId;
        this.eventType = eventType;
        this.message = message;
        this.metadata = metadata;
    }

    public Long getId() { return id; }
    public Long getMissionId() { return missionId; }
    public Instant getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
    public String getMessage() { return message; }
    public String getMetadata() { return metadata; }
}
