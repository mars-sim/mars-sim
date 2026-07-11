/**
 * Mars Simulation Project
 * TesDriveMetaMission.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import java.util.Set;

import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Skeleton implementation of the Meta Mission for a test drive
 */
public class TestDriveMetaMission extends AbstractMetaMission {

    public TestDriveMetaMission() {
        super(MissionType.TEST_DRIVE, 2, Set.of(JobType.PILOT), Set.of(JobType.TECHNICIAN));

        setPreferredVehicle(VehicleType.ROVER_TYPES);
    }

	/**
	 * Constructs an instance of the Test drive
	 * 
	 * @param person the person to perform the mission.
	 * @param vehicle the vehicle to use for the mission.
	 * @param members the other members of the mission.
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 */
    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) {
        return new TestDriveMission("Test drive", crew);
    }
}
