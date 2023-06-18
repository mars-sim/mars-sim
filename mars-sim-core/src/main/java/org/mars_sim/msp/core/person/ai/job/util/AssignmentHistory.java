/*
 * Mars Simulation Project
 * AssignmentHistory.java
 * @date 2023-06-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This records a person's a list of job or role assignment over time
 */
public class AssignmentHistory implements Serializable  {

    private static final long serialVersionUID = 1L;
	
    /** The person's assignment history. */
    private List<Assignment> assignmentList = new ArrayList<>();

	public List<Assignment> getJobAssignmentList() {
		return assignmentList;
	}
    
    /**
     * Saves a pending assignment for a person.
     * 
     * @param newJobStr
     * @param initiator
     * @param status
     * @param approvedBy
     * @param addAssignment
     */
    public void savePendingJob(String newJob, String initiator, AssignmentType status, String approvedBy, boolean addAssignment) {
    	// Note: initiator is "User", status is "Pending", approvedBy is "null", addAssignment is true
        assignmentList.add(new Assignment(newJob.toString(), initiator, status, approvedBy));
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
    	// at the start of sim OR for a pop less than equal 4 settlement in which approvedBy "User"
    	if (assignmentList.isEmpty()) {
     		assignmentList.add(new Assignment(newJob.toString(), initiator, status, approvedBy));
    	}

    	else if (approvedBy.equals(JobUtil.USER)) {
    	   	// user approves the flexible job reassignment (for pop less than 4 only)");
			// Obtain last entry's lastJobStr
     		assignmentList.add(new Assignment(newJob.toString(), initiator, status, approvedBy));
     	}
    	
    	else {
      		int last = assignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		if (approvedBy.equals(JobUtil.SETTLEMENT)){ // based on the emergent need of the settlement
         	   	assignmentList.add(new Assignment(newJob.toString(), initiator, status, approvedBy));
    		}

    		else { 
        		//if status used to be Pending
    			assignmentList.get(last).setAuthorizedBy(approvedBy); 
          		assignmentList.get(last).setStatus(AssignmentType.APPROVED);
     		}
    	}
    }
    
    public void destroy() {
        if (assignmentList != null) 
        	assignmentList.clear();
    }
}
