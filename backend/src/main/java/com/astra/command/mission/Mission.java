package com.astra.command.mission;

import com.astra.command.common.MissionStatus;
import com.astra.command.common.ValidationOutcome;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    private MissionStatus status = MissionStatus.CREATED;
    private String createdBy;
    private String vehicleId = "Astra-SITL-01";
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant validationRequestedAt;
    private Instant validationStartedAt;
    private Instant validationCompletedAt;
    private Instant executionStartedAt;
    private Instant executionCompletedAt;
    @Enumerated(EnumType.STRING)
    private ValidationOutcome validationResult;
    @Column(length = 5000)
    private String validationSummary;
    private double estimatedDistance;
    private double estimatedBatteryUsage;
    private double estimatedBatteryReserve;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    private List<Waypoint> waypoints = new ArrayList<>();

    protected Mission() {
    }

    public Mission(String name, String description, String createdBy, String vehicleId) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.vehicleId = vehicleId == null || vehicleId.isBlank() ? "Astra-SITL-01" : vehicleId;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public void replaceWaypoints(List<Waypoint> newWaypoints) {
        waypoints.clear();
        newWaypoints.stream().sorted(Comparator.comparingInt(Waypoint::getSequence)).forEach(w -> {
            w.attach(this);
            waypoints.add(w);
        });
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public MissionStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public String getVehicleId() { return vehicleId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getValidationRequestedAt() { return validationRequestedAt; }
    public Instant getValidationStartedAt() { return validationStartedAt; }
    public Instant getValidationCompletedAt() { return validationCompletedAt; }
    public Instant getExecutionStartedAt() { return executionStartedAt; }
    public Instant getExecutionCompletedAt() { return executionCompletedAt; }
    public ValidationOutcome getValidationResult() { return validationResult; }
    public String getValidationSummary() { return validationSummary; }
    public double getEstimatedDistance() { return estimatedDistance; }
    public double getEstimatedBatteryUsage() { return estimatedBatteryUsage; }
    public double getEstimatedBatteryReserve() { return estimatedBatteryReserve; }
    public List<Waypoint> getWaypoints() { return waypoints; }

    public void setStatus(MissionStatus status) { this.status = status; }
    public void setValidationRequestedAt(Instant validationRequestedAt) { this.validationRequestedAt = validationRequestedAt; }
    public void setValidationStartedAt(Instant validationStartedAt) { this.validationStartedAt = validationStartedAt; }
    public void setValidationCompletedAt(Instant validationCompletedAt) { this.validationCompletedAt = validationCompletedAt; }
    public void setExecutionStartedAt(Instant executionStartedAt) { this.executionStartedAt = executionStartedAt; }
    public void setExecutionCompletedAt(Instant executionCompletedAt) { this.executionCompletedAt = executionCompletedAt; }
    public void setValidationResult(ValidationOutcome validationResult) { this.validationResult = validationResult; }
    public void setValidationSummary(String validationSummary) { this.validationSummary = validationSummary; }
    public void setEstimatedDistance(double estimatedDistance) { this.estimatedDistance = estimatedDistance; }
    public void setEstimatedBatteryUsage(double estimatedBatteryUsage) { this.estimatedBatteryUsage = estimatedBatteryUsage; }
    public void setEstimatedBatteryReserve(double estimatedBatteryReserve) { this.estimatedBatteryReserve = estimatedBatteryReserve; }
}
