/**
 * Mars Simulation Project
 * LandmarkMetaMission.java
 * @date 2026-07-18
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.environment.LandmarkConfig;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.mission.MissionCreationException;
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
 * Skeleton implementation of the Meta Mission to visit a Landmark
 */
public class LandmarkMetaMission extends AbstractMetaMission {
    
	// Distance in the test drive before turning round
    public static final double TRAVEL_DIST = 100D;

    public LandmarkMetaMission() {
        super(MissionType.VISIT_LANDMARK, 4, Set.of(JobType.PILOT), Collections.emptySet());

        setPreferredVehicle(VehicleType.ROVER_TYPES);
    }

	/**
	 * Constructs an instance of the Landmark visit. This will select a suitable Landmark.
	 * 
	 * @param crew the roster of crew members to perform the mission.
	 * @param needsReview Mission must be reviewed
	 * @return mission instance.
	 * @throws MissionCreationException If no landmark can be found
	 */
    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) throws MissionCreationException {
		Settlement base = crew.leader().getAssociatedSettlement();
        Coordinates startingPlace = base.getCoordinates();

        var range = crew.vehicle().getEstimatedRange();

        LandmarkConfig config = getLandmarkConfig();
        var inScope = config.getLandmarks().getFeatures(startingPlace, 1);
        Landmark selected = inScope.stream()
                .filter(l -> l.getCoordinates().getDistance(startingPlace) < range)
                .findFirst()
                .orElseThrow(() -> new MissionCreationException("mission.landmark.noneinrange"));

        return constructInstance(crew, selected, needsReview);
    }
    
    /**
     * Factory method to create a mission that visits a Landmark with the specified roster.
     * @param roster Roster of members and vehicle
     * @param landmark Landmark to visit
     * @param needsReview Does it need a review
     * @return
     */
    public Mission constructInstance(Roster roster, Landmark landmark, boolean needsReview) {
        Settlement base = roster.leader().getAssociatedSettlement();
        Coordinates startingPlace = base.getCoordinates();

		var mission = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, roster);

        Coordinates turningPoint = landmark.getCoordinates();

        List<MissionStep> plan = new ArrayList<>();
        plan.add(new MissionTravelStep(mission, new NavPoint(turningPoint, landmark.getName(),
                                                            startingPlace)));
        plan.add(new VisitLandmarkStep(mission, landmark));
        plan.add(new MissionTravelStep(mission, new NavPoint(base, turningPoint)));           

        mission.setSteps(plan);  

        return mission;
    }

    private static LandmarkConfig getLandmarkConfig() {
        return SimulationConfig.instance().getLandmarkConfiguration();
    }
}
