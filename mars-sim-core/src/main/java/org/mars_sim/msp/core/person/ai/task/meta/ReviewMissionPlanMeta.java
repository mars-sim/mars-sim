/*
 * Mars Simulation Project
 * ReviewMissionPlanMeta.java
 * @date 2022-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReviewMissionPlan;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.tools.Msg;

/**
 * The meta task for reviewing mission plans.
 */
public class ReviewMissionPlanMeta extends MetaTask implements SettlementMetaTask {
	/**
     * Represents a Job to review a specific mission plan
     */
    private static class ReviewMissionPlanJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private MissionPlanning plan;

        public ReviewMissionPlanJob(SettlementMetaTask owner, MissionPlanning plan, RatingScore score) {
			super(owner, "Review Mission", plan.getMission(), score);
            this.plan = plan;
        }


        @Override
        public Task createTask(Person person) {
            return new ReviewMissionPlan(person, plan);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewMissionPlan"); //$NON-NLS-1$
        
    private static final double BASE_SCORE = 400.0;
	private static final double MAX_SCORE = 750.0;
	private static final int MAX_AGE = 7;
	private static final double SOL_SCORE = (MAX_SCORE - BASE_SCORE) / MAX_AGE;

	private static MissionManager missionManager;
    
    public ReviewMissionPlanMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.LEADERSHIP);
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
			MissionPlanning mp = ((ReviewMissionPlanJob)t).plan;
			Mission m = mp.getMission();			
			int pop = p.getAssociatedSettlement().getNumCitizens();				

			// Is this Person allowed to review this Mission
			if (!p.equals(m.getStartingPerson()) && mp.isReviewerValid(p.getName(), pop)) {
				factor = super.assessPersonSuitability(t, p);
				if (factor.getScore() == 0) {
					return factor;
				}

				// This reviewer is valid
				RoleType roleType = p.getRole().getType();  
				double reviewer = switch(roleType) {
					case MISSION_SPECIALIST -> 1.5;
					case CHIEF_OF_MISSION_PLANNING -> 3;
					case SUB_COMMANDER -> 4.5;
					case COMMANDER -> 6;
					default -> 1;
				};
				factor.addModifier("reviewer", reviewer);
			}
		}
		return factor;
	}

	/**
	 * Scans the Settlement for any Mission that need reviewing.
	 * 
	 * @param settlement Settlement to scan.
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

        for(Mission m : missionManager.getPendingMissions(settlement)) {
        	MissionPlanning mp = m.getPlan();
			if ((mp.getStatus() == PlanType.PENDING) && (mp.getActiveReviewer() == null)) {
				RatingScore score = new RatingScore(BASE_SCORE);               	

				// Add adjustment based on how many sol the request has since been submitted
				// if the job assignment submitted date is > 1 sol
				int sol = getMarsTime().getMissionSol();
				int solRequest = mp.getMissionSol();
				int diff = sol - solRequest;

				// If no one else is able to offer the review after x days, 
				// do allow the review to go through even if the reviewer is not valid
				diff = Math.min(diff, MAX_AGE); // Limit the age of a review
				score.addModifier("review.age", 1 + (diff * SOL_SCORE));

				tasks.add(new ReviewMissionPlanJob(this, mp, score));
			}
		}
	
        return tasks;
    }

	public static void initialiseInstances(MissionManager mm) {
		missionManager = mm;
	}

}