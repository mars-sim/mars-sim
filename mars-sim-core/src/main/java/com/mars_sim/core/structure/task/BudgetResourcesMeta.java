/*
 * Mars Simulation Project
 * BudgetResourcesMeta.java
 * @date 2023-12-02
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

        private static String getGoalName(ReviewGoal goal2) {
			return switch(goal2) {
				case ACCOM_WATER -> "Budget Accomodation Water";
				case RESOURCE -> "Budget Essential Resources";
				case SETTLEMENT_WATER -> "Budget Settlement Water";
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
		addPreferredRole(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES, 2);
		addPreferredRole(RoleType.SUB_COMMANDER, 3);
		addPreferredRole(RoleType.COMMANDER, 4);
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
        
        if (p.isInSettlement() && p.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
			
			factor = super.assessPersonSuitability(t, p);
			if (factor.getScore() == 0) {
				return factor;
			}

			// This reviewer is valid
			RoleType roleType = p.getRole().getType();  
			double reviewer = switch(roleType) {
				case RESOURCE_SPECIALIST -> 1.5;
				case CHIEF_OF_SUPPLY_N_RESOURCES -> 2;
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

		if (settlement.canReviewWaterRatio()) {
			int levelDiff = settlement.getWaterRatioDiff();
			if (levelDiff > 0) {
				RatingScore score = new RatingScore("water.level", levelDiff * BASE_SCORE);
				tasks.add(new BudgetResourcesJob(this, score, 1, ReviewGoal.SETTLEMENT_WATER));
			}
		}
		
		int numResource = settlement.getGoodsManager().getResourceReviewDue();
		if (numResource > 0) { 
			RatingScore score = new RatingScore("resource", BASE_SCORE); 
			tasks.add(new BudgetResourcesJob(this, score, numResource, ReviewGoal.RESOURCE));
		}
		
		int numAcc = getAccommodationNeedingWaterReview(settlement, -1).size();
		if (numAcc > 0) {
			RatingScore score = new RatingScore("waste.water", BASE_SCORE);  
			tasks.add(new BudgetResourcesJob(this, score, numAcc, ReviewGoal.ACCOM_WATER));
           
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