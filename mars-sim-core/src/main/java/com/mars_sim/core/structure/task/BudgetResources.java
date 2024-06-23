/*
 * Mars Simulation Project
 * BudgetResources.java
 * @date 2023-12-02
 * @author Manny Kung
 */
package com.mars_sim.core.structure.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.Administration;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.LivingAccommodation;
import com.mars_sim.core.structure.building.function.Management;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The task for budgeting resources.
 */
public class BudgetResources extends Task {

	public static final double REVIEW_PERC = .9;

	public enum ReviewGoal {RESOURCE, SETTLEMENT_WATER, ACCOM_WATER}
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(BudgetResources.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.budgetResources"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REVIEWING = new TaskPhase(
			Msg.getString("Task.phase.budgetResources.reviewing")); //$NON-NLS-1$

	private static final TaskPhase APPROVING = new TaskPhase(
			Msg.getString("Task.phase.budgetResources.approving")); //$NON-NLS-1$

	// Static members	
	private static final int STANDARD_DURATION = 40;

	// Experience modifier is based on a mixture of abilities
	private static final ExperienceImpact IMPACT = new ExperienceImpact(25D, NaturalAttributeType.EXPERIENCE_APTITUDE,
									false, -0.1D, SkillType.MANAGEMENT) {
										private static final long serialVersionUID = 1L;

										@Override
										protected double getExperienceModifier(Worker worker) {
											int disciplineAptitude = worker.getNaturalAttributeManager().getAttribute(
												NaturalAttributeType.DISCIPLINE);
											int leadershipAptitude = worker.getNaturalAttributeManager().getAttribute(
												NaturalAttributeType.LEADERSHIP);
											return (disciplineAptitude + leadershipAptitude - 100D) / 100D;
										}
									};
	
	// Data members
	/** The administration building the person is using. */
	private Administration office;
	
	private Building building;
	
	private ReviewGoal taskNum;
	
	private int settlementResource;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 * @param goal Optional defintionof the goal
	 */
	public BudgetResources(Person person, ReviewGoal goal) {
		// Use Task constructor.
		super(NAME, person, false, IMPACT, STANDARD_DURATION);
				
		if (person.isInSettlement()) {
		
			if (!selectTask(goal)) {
				endTask();
				return;
			}
			
			int skill = getEffectiveSkillLevel();
			// Duration is skill-dependent	
			setDuration(getDuration() * 2.5 / (1 + skill));
			
			// If person is in a settlement, try to find an office building.
			Building officeBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);

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
				Building managementBuilding = Management.getAvailableStation(person);
				if (managementBuilding != null) {
					// Walk to the management building.
					walkToTaskSpecificActivitySpotInBuilding(managementBuilding, FunctionType.MANAGEMENT, true);
				}
				else {	
					Building dining = BuildingManager.getAvailableDiningBuilding(person, false);
					// Note: dining building is optional
					if (dining != null) {
						// Walk to the dining building.
						walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
					}
				}
			}
		}
		else {
			endTask();
		}

		// Initialize phase
		setPhase(REVIEWING);
	}

	private boolean selectTask(ReviewGoal goal) {
		switch(goal) {
			case ACCOM_WATER:
				return budgetAccommodationWater();
			case RESOURCE:
				return budgetSettlementResource();
			case SETTLEMENT_WATER:
				return budgetSettlementWater();
			default:
				// Evaluate all 3 one by one
				return (budgetSettlementResource()
				|| budgetSettlementWater()
				|| budgetAccommodationWater());
		}
	}
	
	
	private boolean budgetSettlementResource() {
		settlementResource = person.getAssociatedSettlement().getGoodsManager().reserveResourceReview();
		if (settlementResource != -1) {
			taskNum = ReviewGoal.RESOURCE;
			return true;
		}
		
		return false;
	}
	
	private boolean budgetSettlementWater() {

		int levelDiff = person.getAssociatedSettlement().getWaterRatioDiff();
		if (levelDiff > 0) {
			
			taskNum = ReviewGoal.SETTLEMENT_WATER;
			
			// Set the flag to false for future review
			person.getAssociatedSettlement().setReviewWaterRatio(false);
			
			return true;
		}	
		
		return false;
	}
	

	private boolean budgetAccommodationWater() {
		// Pick a building that needs review
		var locn = person.getBuildingLocation();
		int zone = (locn == null ? -1 : locn.getZone());
		var found = BudgetResourcesMeta.getAccommodationNeedingWaterReview(person.getAssociatedSettlement(), zone);
		building = RandomUtil.getRandomElement(found);
					
		if (building != null) {
			
			taskNum = ReviewGoal.ACCOM_WATER;
			// Inform others that this quarters' water ratio review flag is locked
			// and not available for review
			building.getLivingAccommodation().lockWaterRatioReview();
			
			return true;
		}	
		
		return false;
	}
	
	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REVIEWING.equals(getPhase())) {
			return reviewingPhase(time);
		} else if (APPROVING.equals(getPhase())) {
			return approvingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the reviewing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reviewingPhase(double time) {
			
		if (getTimeCompleted() > REVIEW_PERC * getDuration()) {
			switch(taskNum) {
				case ACCOM_WATER: {
					LivingAccommodation quarters = building.getLivingAccommodation();	
					// Calculate the new water ration level
					double[] data = quarters.calculateWaterLevel(time);
					
					logger.log(worker, Level.INFO, 0, "Reviewing " + building.getName()
						+ "'s water ration level.  water: " + Math.round(data[0]*10.0)/10.0
							+ "  Waste water: " + Math.round(data[1]*10.0)/10.0 + ".");
				} break;
				case RESOURCE: {
					person.getAssociatedSettlement().getGoodsManager().checkResourceDemand(settlementResource, time);
				} break;
				case SETTLEMENT_WATER: {
					if (person.getAssociatedSettlement().isWaterRatioChanged()) {
						// Make the new water ratio the same as the cache
						person.getAssociatedSettlement().setWaterRatio();
					}
				} break;
			}

			// Add experience
			addExperience(time);
			
			// Go to the next phase
			setPhase(APPROVING);
		}

        return 0;
	}


	/**
	 * Performs the finished phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double approvingPhase(double time) {
			
		switch(taskNum) {
			case ACCOM_WATER:
				LivingAccommodation quarters = building.getLivingAccommodation();	
				
				// Use water and produce waste water
				quarters.generateWaste(time);
				
				logger.info(worker, 0, "New water waste measures approved for " 
						+ building.getName() + ".");
				break;
			case RESOURCE:
				logger.info(worker, 0, "New resource demand measures approved.");
				break;
			case SETTLEMENT_WATER:
				logger.info(worker, 0, "New water ratio measures approved.");
				break;
		}
			
		// Add experience
		addExperience(time);
		
		// Approval phase is a one shot activity so end task
		endTask();
		
		return 0;
	}

	/**
	 * Releases office space.
	 */
	@Override
	protected void clearDown() {
		super.clearDown();
		
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}
}
