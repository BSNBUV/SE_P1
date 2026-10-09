package com.astra.command.telemetry;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    List<Telemetry> findByMissionIdOrderByTimestampAsc(Long missionId);
    Optional<Telemetry> findTopByMissionIdOrderByTimestampDesc(Long missionId);
    long countByMissionId(Long missionId);
}
