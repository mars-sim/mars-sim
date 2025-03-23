/*
 * Mars Simulation Project
 * ReviewJobReassignmentMeta.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.Assignment;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.ReviewJobReassignment;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * The Meta task for the ReviewJobReassignment task.
 */
public class ReviewJobReassignmentMeta extends MetaTask
	implements SettlementMetaTask
{

	private static class ReviewJobTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        public ReviewJobTaskJob(SettlementMetaTask owner, RatingScore score, int jobAssignments) {
            super(owner, "Review Job Assignment", null, score);
			setDemand(jobAssignments);
        }

        @Override
        public Task createTask(Person person) {
            return new ReviewJobReassignment(person);
        }

        @Override
        public Task createTask(Robot robot) {
            throw new UnsupportedOperationException("Robots cannot review job assignments");
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewJobReassignment"); //$NON-NLS-1$
    
    public ReviewJobReassignmentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.LEADERSHIP);

	}

    /**
     * Gets the score for a Settlement task for a person. 
     * Considers the condition of the person and their current role.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
		RoleType roleType = p.getRole().getType();
		RatingScore factor = RatingScore.ZERO_RATING;

        if (p.isInSettlement() && p.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
			&& (roleType != null)
			&& (roleType.isCouncil()
        	        || roleType.isChief()     			
        			|| (roleType == RoleType.MISSION_SPECIALIST
							&& p.getAssociatedSettlement().getNumCitizens() <= 4))) {
			factor = super.assessPersonSuitability(t, p);
            if (factor.getScore() == 0) {
                return factor;
            }
			
			// Get an available office space.
	        Building building = BuildingManager.getAvailableFunctionTypeBuilding(p, FunctionType.ADMINISTRATION);
			factor = assessBuildingSuitability(factor, building, p);
        }

        return factor;
    }

	/**
	 * Find any JobAssignments that are needed at this Settlement
	 * @param settlement Being reviewed
	 * @return
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		double base = 0D;
		int numOfAssignments = 0;
		int oldestRequest = 0;
		int sol = getMarsTime().getMissionSol();

	    // Get the job history of the candidate not the caller
	    for(Person p : settlement.getAllAssociatedPeople()) {
	        List<HistoryItem<Assignment>> list = p.getJobHistory().getJobAssignmentList();
	        Assignment ja = list.get(list.size()-1).getWhat();
	                    
	        AssignmentType status = ja.getStatus();
	        if (status == AssignmentType.PENDING) {
				numOfAssignments++;
	            
				// Take the base score based on the higher seniority
				RoleType newRole = p.getRole().getType();
				double newBase = switch(newRole) {
					case PRESIDENT -> 600;
					case MAYOR -> 550;
					case COMMANDER -> 500;
					case SUB_COMMANDER -> 450;
					default -> 400;
				};
				base = Math.max(base, newBase);
	
	                    	
				// Add adjustment based on how many sol the request has since been submitted
				// if the job assignment submitted date is > 1 sol
				int solRequest = sol - list.get(list.size()-1).getWhen().getMissionSol();
				oldestRequest = Math.max(solRequest, oldestRequest);
			}
		}

		List<SettlementTask> result = new ArrayList<>();
		if (numOfAssignments > 0) {
			RatingScore score = new RatingScore(base);

			// Addmodifier based on the oldest request; 0.3 per day to a maximum of 3
			score.addModifier("due", 1D + (0.33D * Math.min(3, oldestRequest)));

			result.add(new ReviewJobTaskJob(this, score, numOfAssignments));
		}
		return result;
	}
}
