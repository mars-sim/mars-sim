/*
 * Mars Simulation Project
 * EmergencySupplyMeta.java
 * @date 2026-07-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Meta mission for Emergency Supply missions
 * This does not supports creating a mission via probability, it must be started explitily by the user.
 */
public class EmergencySupplyMeta extends AbstractMetaMission{

    private static final Set<JobType> LEADER_JOBS = JobType.LOADERS;

    public EmergencySupplyMeta() {
        super(MissionType.EMERGENCY_SUPPLY, 2, LEADER_JOBS, LEADER_JOBS);

        setPreferredVehicle(Set.of(VehicleType.CARGO_ROVER));
        setAutomatic(false);
    }

    /**
     * Emergency Supply mission cannot be created automatically, it must be started by the user.
     * Throws UnsupportedOperationException if called.
     */
    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) {
        throw new UnsupportedOperationException("Unimplemented method 'constructInstance'");
    }
}
