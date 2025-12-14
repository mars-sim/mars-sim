/*
 * Mars Simulation Project
 * BudgetResourcesMeta.java
 * @date 2025-08-16
 * @author Manny Kung
 */
package com.mars_sim.core.structure.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.task.BudgetResources.ReviewGoal;
import com.mars_sim.core.tool.Msg;

/**
 * The meta task for budgeting resources.
 */
public class BudgetResourcesMeta extends MetaTask implements SettlementMetaTask {
	/** default logger. */

	/**
     * Represents a Job to review a specific mission plan
     */
    static class BudgetResourcesJob extends SettlementTask {

		private static final long serialVersionUID = 1L;
		private ReviewGoal goal;

        public BudgetResourcesJob(SettlementMetaTask owner, RatingScore score, int demand, ReviewGoal goal) {
			super(owner, getGoalName(goal), null, score);
			setDemand(demand);
			this.goal = goal;
        }

        private static String getGoalName(ReviewGoal goal) {
			return switch(goal) {
				case ICE_RESOURCE -> "Budget Ice Resource";
				case REGOLITH_RESOURCE -> "Budget Regolith Resource";
				case LIFE_RESOURCE -> "Budget Essential Resources";
				case WATER_RATIONING -> "Budget Settlement Water";
			};
		}

		@Override
        public Task createTask(Person person) {
            return new BudgetResources(person, goal);
        }

		public ReviewGoal getGoal() {
			return goal;
		}
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.budgetResources"); //$NON-NLS-1$
        
    private static final double BASE_SCORE = 75.0;

    public BudgetResourcesMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.LEADERSHIP);
		addPreferredRole(RoleType.RESOURCE_SPECIALIST, 1.5D);
		addPreferredRole(RoleType.CHIEF_OF_SUPPLY_RESOURCE, 2);
		addPreferredRole(RoleType.SUB_COMMANDER, 2.5);
		addPreferredRole(RoleType.COMMANDER, 3);
		addPreferredRole(RoleType.DEPUTY_ADMINISTRATOR, 2.5);
		addPreferredRole(RoleType.ADMINISTRATOR, 3);
	}

	/**
     * Gets the score for a Settlement task for a person to review a mission.
     * 
	 * @param t Task being scored
	 * @param p Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
		RoleType roleType = p.getRole().getType(); 
	   	if (RoleType.GUEST == roleType) {
            return factor;
        }
	   	
        if (p.isInSettlement() && p.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
			
			factor = super.assessPersonSuitability(t, p);
			if (factor.getScore() == 0D) {
				return factor;
			}

			// This reviewer is valid 
			double reviewer = switch(roleType) {
				case RESOURCE_SPECIALIST -> 1.5;
				case CHIEF_OF_SUPPLY_RESOURCE -> 2;
				case SUB_COMMANDER -> 3;
				case COMMANDER -> 4;
				default -> 1;
			};
			
			factor.addModifier("reviewer", reviewer);
				
		}
		return factor;
	}

	/**
	 * Scans the settlement for any resources that need reviewing.
	 * 
	 * @param settlement Settlement to scan.
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		if (settlement.getRationing().isReviewDue()) {
			int levelDiff = settlement.getRationing().reviewRationingLevel();
			if (levelDiff != 0) {
				RatingScore score = new RatingScore("water.rationing", BASE_SCORE * Math.abs(levelDiff)/2);
				tasks.add(new BudgetResourcesJob(this, score, 1, ReviewGoal.WATER_RATIONING));
			}
		}
		
		int numResource = settlement.getGoodsManager().getResourceReviewDue();
		if (numResource > 0) { 
			RatingScore score = new RatingScore("resource.lifeSupport", BASE_SCORE); 
			tasks.add(new BudgetResourcesJob(this, score, numResource, ReviewGoal.LIFE_RESOURCE));
		}
		
		boolean iceFlag = settlement.isIceReviewDue();
		if (iceFlag) {
			RatingScore score = new RatingScore("ice.probability", BASE_SCORE);  
			tasks.add(new BudgetResourcesJob(this, score, 1, ReviewGoal.ICE_RESOURCE));
		}
		
		boolean regFlag = settlement.isRegolithReviewDue();
		if (regFlag) {
			RatingScore score = new RatingScore("regolith.probability", BASE_SCORE);  
			tasks.add(new BudgetResourcesJob(this, score, 1, ReviewGoal.REGOLITH_RESOURCE));
		}

		return tasks;
    }

		
	/**
	 * Gets a number of living accommodations that need waste water review.
	 *
	 * @param settlement
	 * @return the number
	 */
	public static List<Building> getAccommodationNeedingWaterReview(Settlement settlement,
				int targetZone) {
		return settlement.getBuildingManager().getBuildingSet(FunctionType.LIVING_ACCOMMODATION)
					.stream()
					.filter(b -> (targetZone < 0) || (b.getZone() == targetZone))
					.filter(b -> !b.getMalfunctionManager().hasMalfunction()
							// True if this quarter needs a review
							&& b.getLivingAccommodation().canReviewWaterRatio())
					.toList();
	}
	
}