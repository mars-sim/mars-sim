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
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.mission.objectives.LandmarkObjective;
import com.mars_sim.core.mission.steps.MissionTravelStep;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.SuppliesManifest;
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
	 * Constructs an instance of the Landmark visit
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
     * Factory method to create a mission that visits a Landmark with teh specified roster.
     * @param roster Roster of memebrs and vehicle
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
        plan.add(new VisitLandmark(mission, landmark));
        plan.add(new MissionTravelStep(mission, new NavPoint(base, turningPoint)));           

        mission.setSteps(plan);  

        return mission;
    }

    private static LandmarkConfig getLandmarkConfig() {
        return SimulationConfig.instance().getLandmarkConfiguration();
    }

    private static class VisitLandmark extends MissionStep {

        private static final int SITE_TIME = 100; // mSol
        private static final long serialVersionUID = 1L;
        private LandmarkObjective objective;

        public VisitLandmark(MissionVehicleProject parent, Landmark landmark) {
            super(parent, Stage.ACTIVE, "Explore " + landmark.getName());

            objective = new LandmarkObjective(landmark, SITE_TIME);
        }

        /**
         * Calculates what resources are needed for this step.
         * 
         * Method should be empty
         * The return value may change once the step is active.
         */
        @Override
        protected void getRequiredResources(SuppliesManifest resources, boolean includeOptionals) {
            addLifeSupportResource(getMission().getMembers().size(), objective.getMSolOnSite(), includeOptionals, resources);
        }

        @Override
        protected boolean execute(Worker worker) {
            if (getStepDuration() > objective.getMSolOnSite()) {
                complete();
            }

            // nothing to do
            return false;
        }
        
        /**
         * Return the objective for visiting a site.
         */
        @Override
        public MissionObjective getObjective() {
            return objective;
        }
    }
}
