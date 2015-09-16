/**
 * Mars Simulation Project
 * JobHistory.java
 * @version 3.08 2015-03-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;

public class JobHistory implements Serializable  {


    private static final long serialVersionUID = 1L;

	//private static transient Logger logger = Logger.getLogger(JobHistory.class.getName());

	private Person person;
	private MarsClock clock;

    /** The person's job history. */
    //private Map<MarsClock, JobAssignment> jobHistoryMap;

    private List<JobAssignment> jobAssignmentList = new CopyOnWriteArrayList<JobAssignment>();

    public JobHistory(Person person) {
		this.person = person;

		jobAssignmentList = new CopyOnWriteArrayList<JobAssignment>();
		//jobHistoryMap = new HashMap<MarsClock, JobAssignment>();

		//jobAssignmentList.add(new JobAssignment(startClock, newJobStr, initiator, status, approvedBy));

	}

	//public Map<MarsClock, JobAssignment> getJobHistoryMap() {
	//	return jobHistoryMap;
	//}

	public List<JobAssignment> getJobAssignmentList() {
		return jobAssignmentList;
	}

    /**
     * Saves the new job assignment for a person.
     */
    // 2015-03-30 Added saveJob()
    public void saveJob(Job newJob, String initiator, String status, String approvedBy, boolean addNewJobAssignment) {
    	//if (job == null) System.out.println("saveJob() : job is null");
    	//if (person.getGender() == null) System.out.println("saveJob() : person.getGender() is null");
    	String newJobStr = newJob.getName(person.getGender());
    	processJob(newJobStr, initiator, status, approvedBy, addNewJobAssignment);
    }


    public void processJob(String newJobStr, String initiator, String status, String approvedBy, boolean addNewJobAssignment) {
    	//System.out.println("calling JobHistory's processJob()");

    	if (jobAssignmentList.isEmpty()) {
    		MarsClock startClock = Simulation.instance().getMasterClock().getInitialMarsTime();
    		jobAssignmentList.add(new JobAssignment(startClock, newJobStr, initiator, status, approvedBy));
    	}

    	else {
    		//System.out.println("JobHistory : jobAssignmentList.size() is currently " + jobAssignmentList.size());
    		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		String lastJobStr = jobAssignmentList.get(last).getJobType();
    		//System.out.println("JobHistory : lastJobStr was " + lastJobStr);
        	if (clock == null)
        		clock = Simulation.instance().getMasterClock().getMarsClock();
    		// Compare lastJobStr with newJobStr
    		if (!lastJobStr.equals(newJobStr)) { // for sanity check
        		//System.out.println("JobHistory : newJobStr was " + newJobStr);
	        	if (addNewJobAssignment) // status.equals("Pending"))
	        		jobAssignmentList.add(new JobAssignment(clock, newJobStr, initiator, status, approvedBy));

	    		else if (status.equals("Approved")) {
	    			String boss = person.getName() + "--" + person.getRole().getType();
	        		jobAssignmentList.get(last).setAuthorizedBy(boss); // or getRole().toString();
	        		//MarsClock clock = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
	            	jobAssignmentList.get(last).setStatus("Approved");
	            	jobAssignmentList.get(last).setTimeAuthorized(clock);
	            	//System.out.println("JobHistory : Just approved by " + jobAssignmentList.get(last).getAuthorizedBy());
	         	    //System.out.println("JobHistory : processJob() : new status is " + jobAssignmentList.get(last).getStatus());
	    		}
        	}
    	}
    }
    /**
     * Saves the new job assignment for a person.
     */
    // 2015-03-30 Added saveJob()
    public void saveJob(String newJobStr, String initiator, String status, String approvedBy, boolean addAssignment) {
    	processJob(newJobStr, initiator, status, approvedBy, addAssignment);
    }

}
