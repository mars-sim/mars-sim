/*
 * Mars Simulation Project
 * PlanMission.java
 * @date 2021-08-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * This class is a task for reviewing mission plans
 */
public class PlanMission extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(PlanMission.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.planMission"); //$NON-NLS-1$

	/** Task phases. */
//	private static final TaskPhase GATHERING = new TaskPhase(Msg.getString("Task.phase.planMission.gatheringData")); //$NON-NLS-1$

	private static final TaskPhase SELECTING = new TaskPhase(Msg.getString("Task.phase.planMission.selectingMission")); //$NON-NLS-1$

	private static final TaskPhase SUBMITTING = new TaskPhase(Msg.getString("Task.phase.planMission.submittingMission")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Data members
	/** The administration building the person is using. */
	private Administration office;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public PlanMission(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 150D + RandomUtil.getRandomInt(-15, 15));

		
		//if (person.isInSettlement() && person.getBuildingLocation().getBuildingType().contains("EVA Airlock")) {
		if (person.isInSettlement()) {

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = Administration.getAvailableOffice(person);

			// Note: office building is optional
			if (officeBuilding != null) {
				office = officeBuilding.getAdministration();	
				if (!office.isFull()) {
					office.addStaff();
					// Walk to the office building.
					walkToTaskSpecificActivitySpotInBuilding(officeBuilding, FunctionType.ADMINISTRATION, true);
				}
			}
			else {
				Building dining = EatDrink.getAvailableDiningBuilding(person, false);
				// Note: dining building is optional
				if (dining != null) {
					// Walk to the dining building.
					walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
				}
				// work anywhere		
			}
			// Note: add other workplace if administration building is not available

		} // end of roleType
		else {
			logger.warning(person, "Not in a Settlement");
			endTask();
		}

		// Initialize phase
		addPhase(SELECTING);

		addPhase(SUBMITTING);
		
		setPhase(SELECTING);
	}	

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (SELECTING.equals(getPhase())) {
			return selectingPhase(time);			
		} else if (SUBMITTING.equals(getPhase())) {
			return submittingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the selecting mission phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double selectingPhase(double time) {
		
		boolean canDo = person.getMind().canStartNewMission();
		
		if (!canDo) {
//			logger.log(person, Level.INFO, 30_000, 
//					"Already joined in a mission. Unable to start a new one at this moment.");
			endTask();
		}
		else {
			logger.log(person, Level.INFO, 30_000, 
					"Looking into the settlement's mission needs.");
			
			// Start a new mission
			person.getMind().getNewMission();
			
			Mission mission = person.getMind().getMission();
			if (mission != null)
				setPhase(SUBMITTING);
			else {
				// No mission found so stop planning for now
				logger.log(person, Level.INFO, 30_000, 
						"Determined that the settlement doesn't need a new mission.");
				endTask();
			}
		}
		
        return 0;
	}
	
	/**
	 * Performs the submitting the mission phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double submittingPhase(double time) {
		
		Mission mission = person.getMind().getMission();
		
		if (mission instanceof VehicleMission && !mission.isDone()) {
			logger.log(worker, Level.INFO, 30_000, "Submitted a mission plan for " + mission.getTypeID() + ".");
			// Flag the mission plan ready for submission
			((VehicleMission)mission).flag4Submission();
				// Note: the plan will go up the chain of command
				// 1. takeAction() in Mind will call mission.performMission(person) 
				// 2. performMission() in Mission will lead to calling  performPhase() in VehicleMission
				// 3. performPhase() in VehicleMission will call requestApprovalPhase() 
				// 4. requestReviewPhase() in VehicleMission will call requestApprovalPhase() in Mission
		}
		
		// Add experience
		addExperience(time); 
		
		endTask();
		
		return 0;
	}
	
	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.EXPERIENCE_APTITUDE);
        int leadershipAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (experienceAptitude + leadershipAptitude- 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        worker.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);

	}

	/**
	 * Release office space
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}

}
