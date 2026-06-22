/**
 * Mars Simulation Project
 * EVAStep.java
 * @date 2026-06-21
 * @author Manny Kung
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionPhase;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.task.EVAOperation.LightLevel;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.vehicle.Vehicle;

public class EVAStep extends MissionStep {
	
	private static final long serialVersionUID = 1L;
	
	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(EVAStep.class.getName());
	
	// Maximum time to wait for sunrise
	protected static final double MAX_WAIT_SUBLIGHT = 400D;

	private static final String NOT_ENOUGH_SUNLIGHT = "EVA - Not Enough Sunlight";
	
	private static final MissionPhase WAIT_SUNLIGHT = new MissionPhase("Mission.phase.waitSunlight");
	private static final MissionStatus EVA_SUIT_CANNOT_BE_LOADED = new MissionStatus("Mission.status.noEVASuits");

    private boolean activeEVA = true;
    
	private int containerID;
	private int containerNum;
	
    private MissionPhase evaPhase;
	private LightLevel minSunlight;
	
	
	protected EVAStep(MissionProject project, Stage stage, String description) {
		super(project, stage, description);
	}

	@Override
	protected boolean execute(Worker worker) {
		MissionProject mp = getMission();
		Vehicle v = null;
		if (mp instanceof MissionVehicleProject vp) {
			v = vp.getVehicle();
		}
		
		if (activeEVA) {
			// Check if crew has been at site for more than one sol.
			double timeDiff = getStepDuration();
//			if (timeDiff > getEstimatedTimeAtEVASite(false)) {
//				logger.info(v, 10_000L, "Ran out of EVA site time.");
//				mp.addMissionLog("No More EVA Site Time", worker.getName());
//				activeEVA = false;
//			}


			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
//			if (activeEVA && hasEmergency()) {
//				logger.info(v, 10_000L, "A medical emergency was reported during the EVA phase of the mission.");
//				mp.addMissionLog("Medical Emergency", worker.getName());
//				activeEVA = false;
//			}

			// Check if enough resources for remaining trip. false = not using margin.
//			if (activeEVA && !hasEnoughResourcesForRemainingMission()) {
//				logger.info(v, 10_000L, "Not enough resources was reported during the EVA phase of the mission.");
//				mp.addMissionLog("Not Enough Resources", worker.getName());
//				activeEVA = false;
//			}
//			
			// All good so far, perform the EVA
//			if (activeEVA) {
//				// performEVA will check if rover capacity is full
//				activeEVA = performEVA((Person) worker);
//			}
		}

		// An EVA-ending event was triggered. End EVA phase.
		if (!activeEVA) {
			// Call everyone back inside
//			endEVATasks();
			
			logger.info(worker, 10_000L, "EVA operation Terminated");
			mp.addMissionLog("EVA Terminated", worker.getName());
			
			// Note: should it end the EVA phase here ?
//			setPhaseEnded(true);
		}
		
		// Check below if anyone has been "teleported"
		if (checkTeleported(worker, mp)) {
			// Note: what to do with those who are teleported ? 
//			Nothing
		}		
		
		return false;
	}
	
	/**
	 * Ensures no "teleported" person is still a member of this mission.
	 * Note: still investigating the cause and how to handle this.
	 * 
	 * @param worker
	 * @param mp
	 */
	boolean checkTeleported(Worker worker, MissionProject mp) {
		boolean result = false;
		getMission();
		
		if (worker instanceof Person p) {
			
			if (!mp.getMembers().contains(p)) {
				logger.severe(p, 10_000, "Teleportation Type 1 detected. Not a mission member.");
				mp.addMissionLog("Teleportation Type 1 - " + p.getName(), worker.getName());
				result = true;
			}
			
			if (p.isInSettlement()) {

				logger.severe(p, 10_000, "Teleportation Type 2 detected. Current location: " 
					+ p.getLocationTag().getExtendedLocation() + ".");
				mp.addMissionLog("Teleportation Type 2 - " + p.getName(), worker.getName());
				// Note: need to debug why this happens and can't remove a person as member yet
					
				result = true;
			}
			
			else if (p.isRightOutsideSettlement()) {

				logger.severe(p, 10_000, "Teleportation Type 3 detected. Current location: " 
					+ p.getLocationTag().getExtendedLocation() + ".");
				mp.addMissionLog("Teleportation Type 3 - " + p.getName(), worker.getName());
				result = true;
			}			
		}
		
		return result;
	}
}
