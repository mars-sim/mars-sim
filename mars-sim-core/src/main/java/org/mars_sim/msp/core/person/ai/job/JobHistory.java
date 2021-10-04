/**
 * Mars Simulation Project
 * JobHistory.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The JobHistory class stores a person's a list of job types/positions over time
 */
public class JobHistory implements Serializable  {

    private static final long serialVersionUID = 1L;

	
    /** The person's job history. */
    private List<JobAssignment> jobAssignmentList = new ArrayList<>();

	public List<JobAssignment> getJobAssignmentList() {
		return jobAssignmentList;
	}


    
    /**
     * Saves a pending new job assignment for a person.
     * 
     * @param newJobStr
     * @param initiator
     * @param status
     * @param approvedBy
     * @param addAssignment
     */
    public void savePendingJob(JobType newJob, String initiator, JobAssignmentType status, String approvedBy, boolean addAssignment) {
    	// Note: initiator is "User", status is "Pending", approvedBy is "null", addAssignment is true
        jobAssignmentList.add(new JobAssignment(newJob, initiator, status, approvedBy));
    }

	/**
	 * Saves the new job assignment for a person.
	 * 
	 * @param newJob
	 * @param initiator
	 * @param status
	 * @param approvedBy
	 * @param addNewJobAssignment
	 */
    public void saveJob(JobType newJob, String initiator, JobAssignmentType status, String approvedBy, boolean addNewJobAssignment) {
    	// at the start of sim OR for a pop less than equal 4 settlement in which approvedBy "User"
    	if (jobAssignmentList.isEmpty()) {
     		jobAssignmentList.add(new JobAssignment(newJob, initiator, status, approvedBy));
    	}

    	else if (approvedBy.equals(JobUtil.USER)) {
    	   	// user approves the flexible job reassignment (for pop less than 4 only)");
			// Obtain last entry's lastJobStr
     		jobAssignmentList.add(new JobAssignment(newJob, initiator, status, approvedBy));
     	}
    	
    	else {
      		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		if (approvedBy.equals(JobUtil.SETTLEMENT)){ // based on the emergent need of the settlement
         	   	jobAssignmentList.add(new JobAssignment(newJob, initiator, status, approvedBy));
    		}

    		else { 
        		//if status used to be Pending
    			jobAssignmentList.get(last).setAuthorizedBy(approvedBy); 
          		jobAssignmentList.get(last).setStatus(JobAssignmentType.APPROVED);
     		}
    	}
    }
    
    public void destroy() {
        if (jobAssignmentList != null) 
        	jobAssignmentList.clear();
    }
}
