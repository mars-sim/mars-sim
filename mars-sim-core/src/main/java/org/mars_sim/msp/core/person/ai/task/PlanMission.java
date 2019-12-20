/**
 * Mars Simulation Project
 * PlanMission.java
 * @version 3.1.0 2019-10-23
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
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

	private static transient Logger logger = Logger.getLogger(PlanMission.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
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
	/** The role of the person who is reviewing the mission plan. */
//	public RoleType roleType;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public PlanMission(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, true, 150D + RandomUtil.getRandomInt(-15, 15));

//		logger.info(person + " was at PlanMission.");
		
//		roleType = person.getRole().getType();
		
		if (person.isInSettlement()) {

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = Administration.getAvailableOffice(person);

			// Note: office building is optional
			if (officeBuilding != null) {
				office = officeBuilding.getAdministration();	
				if (!office.isFull()) {
					office.addStaff();
					// Walk to the office building.
					walkToActivitySpotInBuilding(officeBuilding, true);
				}
			}
			else {
				Building dining = EatDrink.getAvailableDiningBuilding(person, false);
				// Note: dining building is optional
				if (dining != null) {
					// Walk to the dining building.
					walkToActivitySpotInBuilding(dining, true);
				}
//				else {
//					// work anywhere
//				}				
			}
			// TODO: add other workplace if administration building is not available

		} // end of roleType
		else {
			endTask();
		}

		// Initialize phase
		addPhase(SELECTING);
//		addPhase(GATHERING);
		addPhase(SUBMITTING);
		
		setPhase(SELECTING);
	}
	
//	public static boolean isRoleValid(RoleType roleType) {
//		return roleType == RoleType.PRESIDENT || roleType == RoleType.MAYOR
//				|| roleType == RoleType.COMMANDER || roleType == RoleType.SUB_COMMANDER
//				|| roleType == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
//				|| roleType == RoleType.CHIEF_OF_MISSION_PLANNING
//				|| roleType == RoleType.CHIEF_OF_ENGINEERING
//				|| roleType == RoleType.CHIEF_OF_SAFETY_N_HEALTH
//				|| roleType == RoleType.CHIEF_OF_SCIENCE
//				|| roleType == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES
//				|| roleType == RoleType.CHIEF_OF_AGRICULTURE
//				|| roleType == RoleType.MISSION_SPECIALIST;
//	}
	
	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.ADMINISTRATION;
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
			LogConsolidated.log(Level.INFO, 10_000, sourceName, 
					"[" + person.getAssociatedSettlement() + "] " 
			+ person.getName() + " just joined in a mission and was unable to start a new one at this moment.");
			endTask();
		}
		else {
//			LogConsolidated.log(Level.INFO, 10_000, sourceName, 
//					"[" + person.getAssociatedSettlement() + "] " + person.getName() 
//					+ " was looking into the mission needs of the settlement.");
			
			// Start a new mission
			person.getMind().getNewMission();
			
			Mission mission = person.getMind().getMission();
			if (mission != null)
				setPhase(SUBMITTING);
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
		
		if (mission instanceof VehicleMission) {
			LogConsolidated.log(Level.INFO, 0, sourceName, 
					"[" + person.getLocationTag().getQuickLocation() + "] " + person.getName() + " submitted a mission plan for " + mission.toString());
			// Flag the mission plan ready for submission
			((VehicleMission)mission).flag4Submission();
//			mission.setPhase(VehicleMission.REVIEWING);
				// Note: the plan will go up the chain of command
				// 1. takeAction() in Mind will call mission.performMission(person) 
				// 2. performMission() in Mission will lead to calling  performPhase() in VehicleMission
				// 3. performPhase() in VehicleMission will call requestApprovalPhase() 
				// 4. requestReviewPhase() in VehicleMission will call requestApprovalPhase() in Mission
		}
				
//		if (mission != null) {
//			// if the mission is approved/accepted after submission  
//		}
		
		// Add experience
		addExperience(time); 
		
		endTask();
		
		return 0;
	}
	
	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.EXPERIENCE_APTITUDE);
        int leadershipAptitude = person.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (experienceAptitude + leadershipAptitude- 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);

	}

	@Override
	public void endTask() {
		super.endTask();

		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		office = null;
	}
}