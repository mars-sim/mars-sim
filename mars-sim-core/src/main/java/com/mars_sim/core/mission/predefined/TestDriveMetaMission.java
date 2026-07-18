/**
 * Mars Simulation Project
 * TesDriveMetaMission.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.mission.steps.MissionTravelStep;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Skeleton implementation of the Meta Mission for a test drive
 */
public class TestDriveMetaMission extends AbstractMetaMission {
    
	// Distance in the test drive before turning round
    public static final double TRAVEL_DIST = 50D;

    public TestDriveMetaMission() {
        super(MissionType.TEST_DRIVE, 2, Set.of(JobType.PILOT), Set.of(JobType.TECHNICIAN));

        setPreferredVehicle(VehicleType.ROVER_TYPES);
    }

	/**
	 * Constructs an instance of the Test drive
	 * 
	 * @param crew the roster of crew members to perform the mission.
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 */
    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) {

		var name = "Test drive";
		var mission = new MissionVehicleProject(name, MissionType.TEST_DRIVE, 10, crew);

		Settlement base = crew.leader().getAssociatedSettlement();
        Coordinates startingPlace = base.getCoordinates();
        Coordinates turningPoint = startingPlace.getNewLocation(new Direction(0), TRAVEL_DIST);

        List<MissionStep> plan = new ArrayList<>();
        plan.add(new MissionTravelStep(mission, new NavPoint(turningPoint, "Turn around",
                                                            startingPlace)));
        plan.add(new MissionTravelStep(mission, new NavPoint(base, turningPoint)));           

        mission.setSteps(plan);  

        return mission;
    }
}
