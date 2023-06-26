/*
 * Mars Simulation Project
 * AssignmentHistory.java
 * @date 2023-06-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job.util;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.data.History;
import org.mars_sim.msp.core.data.History.HistoryItem;

/**
 * This records a person's a list of job or role assignment over time
 */
public class AssignmentHistory implements Serializable  {

    private static final long serialVersionUID = 1L;
	
    /** The person's assignment history. */
    private History<Assignment> assignmentList = new History<>();

	public List<HistoryItem<Assignment>> getJobAssignmentList() {
		return assignmentList.getChanges();
	}
    
	/**
	 * Saves the new assignment for a person.
	 * 
	 * @param newJob
	 * @param initiator
	 * @param status
	 * @param approvedBy
	 * @param addNewJobAssignment
	 */
    public void saveJob(JobType newJob, String initiator, AssignmentType status, String approvedBy, boolean addNewJobAssignment) {
     	assignmentList.add(new Assignment(newJob, initiator, status, approvedBy));
    }

	/**
	 * Get the last approved job assignment. 
	 * @return
	 */
	public Assignment getLastApproved() {
		List<HistoryItem<Assignment>> history = assignmentList.getChanges();
		int idx = history.size() - 1;
		if (history.get(idx).getWhat().getStatus() == AssignmentType.PENDING) {
			idx--;
		}

		return history.get(idx).getWhat();
	}

    public double getCummulativeJobRating() {
		double score = 0;
		int valid = 0;

		// Count scores ignoring Pending
		for(HistoryItem<Assignment> item : assignmentList.getChanges()) {
			Assignment a = item.getWhat();
			if (a.getStatus() != AssignmentType.PENDING) {
				score += a.getJobRating();
				valid++;
			}
		}
		return score / valid; 
    }
}
