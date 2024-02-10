/*
 * Mars Simulation Project
 * BudgetResources.java
 * @date 2023-12-02
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
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
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;
	
	private static final int STANDARD_DURATION = 40;
	
	// Data members
	/** The administration building the person is using. */
	private Administration office;
	
	private Building building;
	
	private int taskNum;
	
	private int settlementResource;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 * @param target Mission planning to review
	 */
	public BudgetResources(Person person) {
		// Use Task constructor.
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.MANAGEMENT,
				RandomUtil.getRandomInt(20, 30), STANDARD_DURATION);
				
		if (person.isInSettlement()) {
		
			boolean result = selectTask(0);
			
			if (!result) {
				result = selectTask(1);
			}
			
			if (!result) {
				result = selectTask(2);
			}
			
			if (!result) {
				endTask();
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
		addPhase(REVIEWING);
		addPhase(APPROVING);
		
		setPhase(REVIEWING);
	}

	private boolean selectTask(int i) {
		if (i == 0) {
			return budgetSettlementResource();
		}
		else if (i == 1) {
			return budgetSettlementWater();
		}
		else if (i == 2) {
			return budgetAccommodationWater();
		}
		return false;
	}
	
	
	private boolean budgetSettlementResource() {
		settlementResource = person.getAssociatedSettlement().getGoodsManager().reserveResourceReview();
		if (settlementResource != -1) {
			taskNum = 1;
			return true;
		}
		
		return false;
	}
	
	private boolean budgetSettlementWater() {

		int levelDiff = person.getAssociatedSettlement().getWaterRatioDiff();
		if (levelDiff > 0) {
			
			taskNum = 2;
			
			// Set the flag to false for future review
			person.getAssociatedSettlement().setReviewWaterRatio(false);
			
			return true;
		}	
		
		return false;
	}
	

	private boolean budgetAccommodationWater() {
		// Pick a building that needs review
		building = BuildingManager.getLivingAccommodationNeedingReview(person);
					
		if (building != null) {
			
			taskNum = 0;
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
			
		if (getTimeCompleted() > .9 * getDuration()) {
			switch(taskNum) {
				case 0: {
					LivingAccommodation quarters = building.getLivingAccommodation();	
					// Calculate the new water ration level
					double[] data = quarters.calculateWaterLevel(time);
					
					logger.log(worker, Level.INFO, 0, "Reviewing " + building.getName()
						+ "'s water ration level.  water: " + Math.round(data[0]*10.0)/10.0
							+ "  Waste water: " + Math.round(data[1]*10.0)/10.0);
				} break;
				case 1: {
					person.getAssociatedSettlement().getGoodsManager().checkResourceDemand(settlementResource, time);
				} break;
				case 2: {
					if (person.getAssociatedSettlement().isWaterRatioChanged()) {
						// Make the new water ratio the same as the cache
						person.getAssociatedSettlement().setWaterRatio();
					}
				} break;
				default:
					logger.warning(person, "Cannot process taskNum " + taskNum);
					endTask();
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
	 * @return th)e amount of time (millisols) left over after performing the phase.
	 */
	private double approvingPhase(double time) {
			
		if (taskNum == 0) {
			LivingAccommodation quarters = building.getLivingAccommodation();	
			
			// Use water and produce waste water
			quarters.generateWaste(time);
			
			logger.info(worker, 0, "New water waste measures approved for " 
					+ building.getName() + ".");
		}
		else if (taskNum == 1) {
			logger.info(worker, 0, "New resource demand measures approved.");
		}
		else if (taskNum == 2) {
			logger.info(worker, 0, "New water ratio measures approved.");
		}
			
		// Add experience
		addExperience(time);
		
		// Approval phase is a one shot activity so end task
		endTask();
		
		return 0;
	}

	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int disciplineAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.DISCIPLINE);
        int leadershipAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (disciplineAptitude + leadershipAptitude - 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        worker.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);
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
