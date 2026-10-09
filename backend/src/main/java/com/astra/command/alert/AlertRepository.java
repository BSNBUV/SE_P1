package com.astra.command.alert;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findTop20ByOrderByTimestampDesc();
    List<Alert> findByMissionIdOrderByTimestampAsc(Long missionId);
    long countByMissionId(Long missionId);
}
