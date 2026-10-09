package com.astra.command.log;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionLogRepository extends JpaRepository<MissionLog, Long> {
    List<MissionLog> findTop20ByOrderByTimestampDesc();
    List<MissionLog> findByMissionIdOrderByTimestampAsc(Long missionId);
}
