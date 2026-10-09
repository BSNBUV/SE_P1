package com.astra.command.validation;

import com.astra.command.common.MissionStatus;
import com.astra.command.log.MissionLog;
import com.astra.command.log.MissionLogRepository;
import com.astra.command.mission.Mission;
import com.astra.command.mission.MissionRepository;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidationJobService {
    private final MissionRepository missions;
    private final MissionLogRepository logs;
    private final ValidationEngine validationEngine;
    private final StringRedisTemplate redisTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public ValidationJobService(MissionRepository missions, MissionLogRepository logs,
                                ValidationEngine validationEngine, StringRedisTemplate redisTemplate) {
        this.missions = missions;
        this.logs = logs;
        this.validationEngine = validationEngine;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Mission submit(Long missionId) {
        Mission mission = missions.findById(missionId).orElseThrow();
        mission.setStatus(MissionStatus.VALIDATING);
        mission.setValidationRequestedAt(Instant.now());
        logs.save(new MissionLog(missionId, "VALIDATION_QUEUED", "Validation job queued", "{\"queue\":\"redis-backed\"}"));
        missions.saveAndFlush(mission);
        try {
            redisTemplate.opsForList().rightPush("astra:validation:jobs", missionId.toString());
        } catch (Exception ignored) {
            logs.save(new MissionLog(missionId, "REDIS_UNAVAILABLE", "Redis queue write failed; in-process worker accepted the job", "{}"));
        }
        executor.submit(() -> run(missionId));
        return mission;
    }

    @Transactional
    public void run(Long missionId) {
        Mission mission = missions.findById(missionId).orElseThrow();
        mission.setValidationStartedAt(Instant.now());
        ValidationResult result = validationEngine.validate(mission);
        mission.setValidationResult(result.overall());
        mission.setValidationSummary(result.summary());
        mission.setEstimatedDistance(result.distanceKm());
        mission.setEstimatedBatteryUsage(result.estimatedBatteryUsage());
        mission.setEstimatedBatteryReserve(result.estimatedBatteryReserve());
        mission.setValidationCompletedAt(Instant.now());
        mission.setStatus(result.overall() == com.astra.command.common.ValidationOutcome.REJECT
                ? MissionStatus.CREATED
                : MissionStatus.VALIDATED);
        missions.save(mission);
        logs.save(new MissionLog(missionId, "VALIDATION_COMPLETED", "Validation finished with " + result.overall(), result.summary()));
    }
}
