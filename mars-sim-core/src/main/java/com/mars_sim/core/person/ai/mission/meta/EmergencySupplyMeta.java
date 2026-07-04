/*
 * Mars Simulation Project
 * EmergencySupplyMeta.java
 * @date 2026-07-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MissionType;

/**
 * Meta mission for Emergency Supply missions
 * This does not supports creating a mission via probability, it must be started explitily by the user.
 */
public class EmergencySupplyMeta extends AbstractMetaMission{

    private static final Set<JobType> LEADER_JOBS = JobType.LOADERS;

    EmergencySupplyMeta() {
        super(MissionType.EMERGENCY_SUPPLY, 2, LEADER_JOBS, LEADER_JOBS);
    }
}
