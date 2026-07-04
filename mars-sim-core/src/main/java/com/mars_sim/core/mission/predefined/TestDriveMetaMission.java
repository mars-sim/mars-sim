/**
 * Mars Simulation Project
 * TesDriveMetaMission.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.AbstractMetaMission;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Skeleton implementation of the Meta Mission for a test drive
 */
public class TestDriveMetaMission extends AbstractMetaMission {

    public TestDriveMetaMission() {
        super(MissionType.TEST_DRIVE, 2, Set.of(JobType.PILOT), Set.of(JobType.TECHNICIAN));

        setPreferredVehicle(VehicleType.ROVER_TYPES);
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new TestDriveMission("Test drive", person);
    }
}
