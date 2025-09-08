/*
 * Mars Simulation Project
 * BudgetResources.java
 * @date 2025-08-16
 * @author Manny Kung
 */
package com.mars_sim.core.structure.task;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.Administration;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Management;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;

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

	private static final TaskPhase SUBMITTING = new TaskPhase(
			Msg.getString("Task.phase.budgetResources.submitting")); //$NON-NLS-1$

	// Static members	
	private static final int STANDARD_DURATION = 50;

	public static final double REVIEW_PERC = .9;

	public static enum ReviewGoal {LIFE_RESOURCE, WATER_RATIONING, ICE_RESOURCE, REGOLITH_RESOURCE}
	
	// Experience modifier is based on a mixture of abilities
	private static final ExperienceImpact IMPACT = new ExperienceImpact(25D, NaturalAttributeType.EXPERIENCE_APTITUDE,
		false, 0.2D, SkillType.MANAGEMENT) {
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
	private int settlementResource;
	
	private double newValue = 0;
	/** The administration building the person is using. */
	private Administration office;

	private ReviewGoal taskNum;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 * @param goal Optional definition of the goal
	 */
	public BudgetResources(Person person, ReviewGoal goal) {
		// Use Task constructor.
		super(NAME, person, false, IMPACT, STANDARD_DURATION);
				
		if (person.isInSettlement()) {
		
			if (!selectTask(goal)) {
				endTask();
				return;
			}
			
			int effectiveSkillLevel = getEffectiveSkillLevel();
			// Duration is skill-dependent	
			setDuration(getDuration() / (1 + effectiveSkillLevel));
			
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


	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REVIEWING.equals(getPhase())) {
			return reviewingPhase(time);
		} else if (SUBMITTING.equals(getPhase())) {
			return submittingPhase(time);
		} else {
			return time;
		}
	}

	/*
	 * Selects a task.
	 * 
	 * @param goal
	 */
	private boolean selectTask(ReviewGoal goal) {
		switch(goal) {
			case ICE_RESOURCE:
				return budgetIceResource();
			case REGOLITH_RESOURCE:
				return budgetRegolithResource();
			case LIFE_RESOURCE:
				return budgetSettlementResource();
			case WATER_RATIONING:
				return budgetSettlementWater();
			default:
				// Evaluate all 3 one by one
				return (budgetSettlementResource()
				|| budgetSettlementWater()
				|| budgetIceResource()
				|| budgetRegolithResource());
		}
	}
	
	/**
	 * Budgets a settlement resource.
	 * 
	 * @return
	 */
	private boolean budgetSettlementResource() {
		settlementResource = person.getAssociatedSettlement().getGoodsManager().reserveResourceReview();
		if (settlementResource != -1) {
			taskNum = ReviewGoal.LIFE_RESOURCE;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Budgets settlement water.
	 * 
	 * @return
	 */
	private boolean budgetSettlementWater() {
		
		int levelDiff = person.getAssociatedSettlement().getRationing().getLevelDiff();
		if (levelDiff != 0) {
			
			taskNum = ReviewGoal.WATER_RATIONING;
			
			// Set the flag to false to prevent another person from starting a review 
			// while this person is about to review it
			person.getAssociatedSettlement().getRationing().setReviewDue(false);
			
			return true;
		}	
		
		return false;
	}
	
	/**
	 * Budgets the ice resource.
	 * 
	 * @return
	 */
	private boolean budgetIceResource() {
		taskNum = ReviewGoal.ICE_RESOURCE;
	
		return true;
	}
	
	/**
	 * Budgets the regolith resource.
	 * 
	 * @return
	 */
	private boolean budgetRegolithResource() {
		taskNum = ReviewGoal.REGOLITH_RESOURCE;
	
		return true;
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
				case ICE_RESOURCE: {
					newValue = person.getAssociatedSettlement().reviewIce();
				} break;
				
				case REGOLITH_RESOURCE: {
					newValue = person.getAssociatedSettlement().reviewRegolith();
				} break;
				
				case LIFE_RESOURCE: {				
					newValue = person.getAssociatedSettlement().getGoodsManager().moderateLifeResourceDemand(settlementResource);					
				} break;
				
				case WATER_RATIONING: {			
					newValue = person.getAssociatedSettlement().getRationing().reviewRationingLevel();
				} break;
			}

			// Add experience
			addExperience(getTimeCompleted());
		
			// Go to the next phase
			setPhase(SUBMITTING);
		}
		
        return 0;
	}


	/**
	 * Performs the finished phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double submittingPhase(double time) {
			
		switch(taskNum) {
			case ICE_RESOURCE:
				if (newValue != 0) {
					person.getAssociatedSettlement().setIceReviewDue(false);
					person.getAssociatedSettlement().setIceApprovalDue(true);
					
					logger.info(worker, 30_000, "Submitting a new ice probability for the settlement.");	
				}
//				else {
//					logger.info(worker, 30_000, "No need to change the ice probability.");	
//				}
				break;
				
			case REGOLITH_RESOURCE:
				if (newValue != 0) {
					person.getAssociatedSettlement().setRegolithReviewDue(false);
					person.getAssociatedSettlement().setRegolithApprovalDue(true);
					
					logger.info(worker, 30_000, "Submitting a new regolith probability for the settlement.");	
				}
//				else {
//					logger.info(worker, 30_000, "No need to change the regolith probability.");	
//				}

				break;
				
			case LIFE_RESOURCE:
				
				if (newValue > 0) {
					
					person.getAssociatedSettlement().getGoodsManager().injectResourceDemand(settlementResource, newValue);
					
					person.getAssociatedSettlement().getGoodsManager().updateOneGood(GoodsUtil.getGood(settlementResource));
					
					logger.info(worker, 30_000, "Submitting a new resource demand measure for the settlement.");	
				}
//				else {
//					logger.info(worker, 30_000, "No need to change the resource demand for the settlement.");	
//				}
				
				break;
				
			case WATER_RATIONING:
				
				if (newValue != 0) {		
					// Submit request and ask for approval
					person.getAssociatedSettlement().getRationing().setApprovalDue(true);
				}
				
				logger.info(worker, 30_000, "Submitting a new water rationing measure for the settlement.");
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
	
	/**
	 * Does it need to inject demand on essential resource ?
	 * 
	 * @return
	 */
	public boolean injectDemand() {
		return newValue > 0;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		office = null;
		taskNum = null;
	}
	
}
