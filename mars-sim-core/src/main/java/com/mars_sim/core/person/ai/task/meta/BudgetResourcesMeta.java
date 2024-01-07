/*
 * Mars Simulation Project
 * BudgetResourcesMeta.java
 * @date 2023-12-02
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.BudgetResources;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.tools.Msg;

/**
 * The meta task for budgeting resources.
 */
public class BudgetResourcesMeta extends MetaTask implements SettlementMetaTask {
	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(BudgetResourcesMeta.class.getName());

	/**
     * Represents a Job to review a specific mission plan
     */
    private static class BudgetResourcesJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public BudgetResourcesJob(SettlementMetaTask owner, RatingScore score) {
			super(owner, "Budget Resources", null, score);
        }

        @Override
        public Task createTask(Person person) {
            return new BudgetResources(person);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.budgetResources"); //$NON-NLS-1$
        
    private static final double BASE_SCORE = 20.0;

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

		RatingScore score = new RatingScore();  
		int levelDiff = 0;
		
		boolean canReview = settlement.canReviewWaterRatio();
		
		boolean changed = settlement.isWaterRatioChanged();
		
		if (canReview) {
			
			changed = settlement.isWaterRatioChanged();
			if (changed) {
				
				levelDiff = settlement.getWaterRatioDiff();
				if (levelDiff > 0) { 
					score.addBase("water.level", levelDiff * BASE_SCORE); 
				}
			}
		}
		
		int numResource = settlement.findNumResourceReviewDue();
		if (numResource > 0) { 
			score.addBase("resource", numResource * BASE_SCORE); 
		}
		
		int numAcc = BuildingManager.getNumAccommodationNeedingReview(settlement);

		if (numAcc > 0) {
			score.addBase("waste.water", numAcc * BASE_SCORE);             
		}
		
		if (levelDiff == 0 && numResource == 0 && numAcc == 0) {
			return Collections.emptyList();
		}
		else {
			tasks.add(new BudgetResourcesJob(this, score));
			return tasks;
		}
    }
}