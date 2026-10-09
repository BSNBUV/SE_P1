package com.astra.command.config;

import com.astra.command.auth.AppUser;
import com.astra.command.auth.AppUserRepository;
import com.astra.command.common.Role;
import com.astra.command.mission.Mission;
import com.astra.command.mission.MissionRepository;
import com.astra.command.mission.Waypoint;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(AppUserRepository users, MissionRepository missions, PasswordEncoder encoder) {
        return args -> {
            if (users.count() == 0) {
                users.save(new AppUser("admin", encoder.encode("AstraAdmin!2026"), Role.ADMIN));
                users.save(new AppUser("operator", encoder.encode("AstraOperator!2026"), Role.OPERATOR));
            }
            if (missions.count() == 0) {
                Mission safe = new Mission("SAFE_DEMO_MISSION", "Deterministic campus-style route", "admin", "Astra-SITL-01");
                safe.replaceWaypoints(List.of(
                        new Waypoint(1, 12.97160, 77.59460, 40),
                        new Waypoint(2, 12.97300, 77.59610, 45),
                        new Waypoint(3, 12.97420, 77.59380, 42),
                        new Waypoint(4, 12.97210, 77.59270, 38)));
                missions.save(safe);

                Mission low = new Mission("LOW_BATTERY_DEMO", "Long route intended to warn on battery reserve", "admin", "Astra-SITL-01");
                low.replaceWaypoints(List.of(
                        new Waypoint(1, 12.97160, 77.59460, 80),
                        new Waypoint(2, 13.04000, 77.66000, 90),
                        new Waypoint(3, 13.11000, 77.72000, 90),
                        new Waypoint(4, 13.18000, 77.78000, 85)));
                missions.save(low);

                Mission invalid = new Mission("INVALID_ROUTE_DEMO", "Duplicate and unsafe waypoint route", "admin", "Astra-SITL-01");
                invalid.replaceWaypoints(List.of(
                        new Waypoint(1, 12.97160, 77.59460, 40),
                        new Waypoint(2, 12.97160, 77.59460, 40),
                        new Waypoint(3, 12.97170, 77.59480, 250)));
                missions.save(invalid);
            }
        };
    }
}
