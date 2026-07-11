/*
 * Mars Simulation Project
 * MissionGenerator.java
 * @date 2026-07-11
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MetaMissionRegistry;

/**
 * Generates help output for Mission Meta definitions.
 */
public class MissionGenerator extends TypeGenerator<MetaMission> {

    public static final String TYPE_NAME = "mission";

    protected MissionGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Mission",
                "Definitions of mission types and their planning constraints.",
                null);
    }

    @Override
    protected void addEntityProperties(MetaMission mission, Map<String, Object> scope) {
        var leaderJobs = mission.getPreferredLeaderJob().stream()
                .map(j -> j.getName())
                .sorted()
                .toList();
        scope.put("leaderJobs", leaderJobs);
        scope.put("hasLeaderJobs", !leaderJobs.isEmpty());

        var workerJobs = mission.getPreferredWorkerJobs().stream()
                .map(j -> j.getName())
                .sorted()
                .toList();
        scope.put("workerJobs", workerJobs);
        scope.put("hasWorkerJobs", !workerJobs.isEmpty());

        var preferredVehicles = mission.getPreferredVehicle().stream()
                .map(v -> v.getName())
                .sorted()
                .toList();
        scope.put("preferredVehicles", preferredVehicles);
        scope.put("hasPreferredVehicles", !preferredVehicles.isEmpty());

        var preferredRobots = mission.getPreferredRobots().stream()
                .map(r -> r.getName())
                .sorted()
                .toList();
        scope.put("preferredRobots", preferredRobots);
        scope.put("hasPreferredRobots", !preferredRobots.isEmpty());
    }

    @Override
    protected List<MetaMission> getEntities() {
        return MetaMissionRegistry.getMetaMissions().stream()
                .sorted(Comparator.comparing(MetaMission::getName))
                .toList();
    }

    @Override
    protected String getEntityName(MetaMission mission) {
        return mission.getName();
    }
}