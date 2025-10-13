/*
 * Mars Simulation Project
 * ApproveMeasuresMeta.java
 * @date 2025-08-16
 * @author Manny Kung
 */
package com.mars_sim.core.structure.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.task.ApproveMeasures.ReviewGoal;
import com.mars_sim.core.tool.Msg;

/**
 * The meta task for approving measures.
 */
public class ApproveMeasuresMeta extends MetaTask implements SettlementMetaTask {
	
	/** default logger. */

	/**
     * Represents a job to approve measures.
     */
    static class ApproveMeasuresJob extends SettlementTask {

		private static final long serialVersionUID = 1L;
		private ReviewGoal goal;

        public ApproveMeasuresJob(SettlementMetaTask owner, RatingScore score, int demand, ReviewGoal goal) {
			super(owner, getGoalName(goal), null, score);
			setDemand(demand);
			this.goal = goal;
        }

        private static String getGoalName(ReviewGoal goal) {
			return switch(goal) {
				case WATER_RATIONING -> "Approve Water Rationing Measure";
				case ICE_RESOURCE -> "Approve Ice Resource Probability";
				case REGOLITH_RESOURCE -> "Approve Regolith Resource Probability";
			};
		}

		@Override
        public Task createTask(Person person) {
            return new ApproveMeasures(person, goal);
        }

		public ReviewGoal getGoal() {
			return goal;
		}
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.approveMeasures"); //$NON-NLS-1$
        
    private static final double BASE_SCORE = 75.0;

    public ApproveMeasuresMeta() {
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

		if (settlement.getRationing().isApprovalDue()) {
//			int levelDiff = settlement.getRationing().getLevelDiff();
//			if (levelDiff != 0) {
				RatingScore score = new RatingScore("water.rationing", 5 * BASE_SCORE);
				tasks.add(new ApproveMeasuresJob(this, score, 1, ReviewGoal.WATER_RATIONING));
//			}
		}
		
		boolean iceFlag = settlement.isIceApprovalDue();
		if (iceFlag) {
			RatingScore score = new RatingScore("ice.probability", 5 * BASE_SCORE);  
			tasks.add(new ApproveMeasuresJob(this, score, 1, ReviewGoal.ICE_RESOURCE));
		}

		boolean regFlag = settlement.isRegolithApprovalDue();
		if (regFlag) {
			RatingScore score = new RatingScore("regolith.probability", 5 * BASE_SCORE);  
			tasks.add(new ApproveMeasuresJob(this, score, 1, ReviewGoal.REGOLITH_RESOURCE));
		}
		
		return tasks;
    }
}