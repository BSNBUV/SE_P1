package com.astra.command.adapter;

import com.astra.command.simulation.SimulatorService;
import org.springframework.stereotype.Component;

@Component
public class SimulatedSITLAdapter implements MissionAdapter {
    private final SimulatorService simulatorService;

    public SimulatedSITLAdapter(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @Override
    public String name() {
        return "SimulatedSITLAdapter";
    }

    @Override
    public void execute(CommonMission mission) {
        simulatorService.start(mission);
    }

    @Override
    public void pause(Long missionId) {
        simulatorService.pause(missionId);
    }

    @Override
    public void resume(Long missionId) {
        simulatorService.resume(missionId);
    }

    @Override
    public void emergencyStop(Long missionId) {
        simulatorService.emergencyStop(missionId);
    }

    @Override
    public void simulateLowBattery(Long missionId) {
        simulatorService.simulateLowBattery(missionId);
    }

    @Override
    public void simulateConnectionLoss(Long missionId) {
        simulatorService.simulateConnectionLoss(missionId);
    }

    @Override
    public void reset(Long missionId) {
        simulatorService.reset(missionId);
    }

    @Override
    public String connectionStatus(Long missionId) {
        return simulatorService.connectionStatus(missionId);
    }
}
