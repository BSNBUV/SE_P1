package com.astra.command.alert;

import com.astra.command.common.AlertSeverity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long missionId;
    private Instant timestamp = Instant.now();
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;
    private String type;
    @Column(length = 2000)
    private String message;
    private boolean acknowledged;

    protected Alert() {
    }

    public Alert(Long missionId, AlertSeverity severity, String type, String message) {
        this.missionId = missionId;
        this.severity = severity;
        this.type = type;
        this.message = message;
    }

    public Long getId() { return id; }
    public Long getMissionId() { return missionId; }
    public Instant getTimestamp() { return timestamp; }
    public AlertSeverity getSeverity() { return severity; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public boolean isAcknowledged() { return acknowledged; }
}
