package com.astra.command.adapter;

import com.astra.command.mission.Mission;
import com.astra.command.mission.Waypoint;
import java.util.List;

public record CommonMission(Long id, String name, String vehicleId, List<CommonWaypoint> waypoints) {
    public static CommonMission from(Mission mission) {
        return new CommonMission(
                mission.getId(),
                mission.getName(),
                mission.getVehicleId(),
                mission.getWaypoints().stream()
                        .map(w -> new CommonWaypoint(w.getSequence(), w.getLatitude(), w.getLongitude(), w.getAltitude()))
                        .toList());
    }

    public record CommonWaypoint(int sequence, double latitude, double longitude, double altitude) {
    }
}
