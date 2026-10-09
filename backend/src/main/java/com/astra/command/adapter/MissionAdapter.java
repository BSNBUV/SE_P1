package com.astra.command.adapter;

public interface MissionAdapter {
    String name();
    void execute(CommonMission mission);
    void pause(Long missionId);
    void resume(Long missionId);
    void emergencyStop(Long missionId);
    void simulateLowBattery(Long missionId);
    void simulateConnectionLoss(Long missionId);
    void reset(Long missionId);
    String connectionStatus(Long missionId);
}
