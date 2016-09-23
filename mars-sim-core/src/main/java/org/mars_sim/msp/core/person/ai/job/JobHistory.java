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
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The JobHistory class stores a person's a list of job types/positions over time
 */
public class JobHistory implements Serializable  {

    private static final long serialVersionUID = 1L;

	//private static transient Logger logger = Logger.getLogger(JobHistory.class.getName());
    private int solCache;
	private Person person;
	private MarsClock clock;
	private MasterClock masterClock;
	
	
    /** The person's job history. */
    //private Map<MarsClock, JobAssignment> jobHistoryMap;

    private List<JobAssignment> jobAssignmentList = new CopyOnWriteArrayList<JobAssignment>();

    public JobHistory(Person person) {
		this.person = person;

		jobAssignmentList = new CopyOnWriteArrayList<JobAssignment>();
		//jobHistoryMap = new HashMap<MarsClock, JobAssignment>();

		//jobAssignmentList.add(new JobAssignment(startClock, newJobStr, initiator, status, approvedBy));

		masterClock = Simulation.instance().getMasterClock();
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
	// Called by assignJob() in Mind.java
    public void saveJob(Job newJob, String initiator, JobAssignmentType status, String approvedBy, boolean addNewJobAssignment) {
    	//System.out.println("\n< " + person.getName() + " > ");
    	//System.out.println("JobHistory.java : starting saveJob() & calling addJob()");
    	String newJobStr = newJob.getName(person.getGender());
    	addJob(newJobStr, initiator, status, approvedBy, addNewJobAssignment);
    }

    /**
     * Saves a pending new job assignment for a person.
     */
    // 2015-03-30 Added saveJob()
    // 2015-09-23 Renamed saveJob() to savePendingJob()
    // Called by TabPanelCareer's actionPerformed()
    public void savePendingJob(String newJobStr, String initiator, JobAssignmentType status, String approvedBy, boolean addAssignment) {
    	//System.out.println("\n< " + person.getName() + " > ");
    	//System.out.println("JobHistory.java : savePendingJob() : starting");
    	// Note: initiator is "User", status is "Pending", approvedBy is "null", addAssignment is true
   		//System.out.println("JobHistory.java : jobAssignmentList's size was " + jobAssignmentList.size());

  		//if (status.equals(JobAssignmentType.PENDING)) { // sanity check okay
	   		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
			String lastJobStr = jobAssignmentList.get(last).getJobType();
			//System.out.println("JobHistory.java : pending newJobStr = " + newJobStr + "   lastJobStr = " + lastJobStr);
        	//System.out.println("JobHistory.java : status is Pending");

        	if (clock == null)
        		clock = masterClock.getMarsClock();
			jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
        	jobAssignmentList.get(last).setSolSubmitted();
			//System.out.println("JobHistory.java : jobAssignmentList's size is now " + jobAssignmentList.size());
  		//}
    }

    /**
     * Adds the new job to the job assignment list
     */
    //2015-09-23 Renamed processJob to addJob()
    public void addJob(String newJobStr, String initiator, JobAssignmentType status, String approvedBy, boolean addNewJobAssignment) {
    	//System.out.println("JobHistory.java : addJob() : starting");
    	//System.out.println("JobHistory.java : jobAssignmentList's size was " + jobAssignmentList.size());
    	// at the start of sim OR for a pop <= 4 settlement in which approvedBy = "User"
    	if (jobAssignmentList.isEmpty()) {
    	   	//System.out.println("JobHistory.java : addJob() : jobAssignmentList was empty. Adding the first job");
    		//MarsClock startClock = Simulation.instance().getMasterClock().getInitialMarsTime();
        	if (clock == null)
        		clock = masterClock.getMarsClock();
    		jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
      		//System.out.println("JobHistory.java : newJobStr = " + newJobStr);
    		//System.out.println("JobHistory.java : jobAssignmentList's size is now " + jobAssignmentList.size());
    	}

    	else if (approvedBy.equals(JobManager.USER)) {
    	   	//System.out.println("JobHistory.java : addJob() : the user approves the flexible job reassignment (for pop <= 4 only)");
    		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		String lastJobStr = jobAssignmentList.get(last).getJobType();
    		//System.out.println("JobHistory.java : newJobStr = " + newJobStr + "   lastJobStr = " + lastJobStr);
        	if (clock == null)
        		clock = masterClock.getMarsClock();
    		jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
        	jobAssignmentList.get(last).setSolSubmitted();
    		//System.out.println("JobHistory.java : newJobStr = " + newJobStr);
    		//System.out.println("JobHistory.java : jobAssignmentList's size is now " + jobAssignmentList.size());
    	}
    	else {
    		//System.out.println("JobHistory : jobAssignmentList.size() is currently " + jobAssignmentList.size());
    		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		String lastJobStr = jobAssignmentList.get(last).getJobType();
    		//System.out.println("JobHistory : lastJobStr was " + lastJobStr);

    		//System.out.println("JobHistory.java : newJobStr = " + newJobStr + "   lastJobStr = " + lastJobStr);

    		if (approvedBy.equals(JobManager.SETTLEMENT)){ // based on the emergent need of the settlement
        	   	//System.out.println("JobHistory.java : addJob() : approving the job reassignment as dictated by settlement's need ");
        		//MarsClock startClock = Simulation.instance().getMasterClock().getInitialMarsTime();
            	if (clock == null)
            		clock = masterClock.getMarsClock();
        	   	jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
            	jobAssignmentList.get(last).setSolSubmitted();
        	   	//System.out.println("JobHistory.java : jobAssignmentList's size is now " + jobAssignmentList.size());
    		}

    		else { //if (status.equals(JobAssignmentType.APPROVED)) { // same as checking if addNewJobAssignment is false
        		//if status used to be Pending
    			jobAssignmentList.get(last).setAuthorizedBy(approvedBy); // or getRole().toString();
            	//System.out.println("JobHistory.java : set status to approved");
        		jobAssignmentList.get(last).setStatus(JobAssignmentType.APPROVED);
            	if (clock == null)
            		clock = masterClock.getMarsClock();
            	//jobAssignmentList.get(last).setTimeAuthorized(clock);
            	//System.out.println("JobHistory.java : Just approved by " + approvedBy);
            	//System.out.println("JobHistory : addJob() : new status is " + jobAssignmentList.get(last).getStatus());
        		//System.out.println("JobHistory.java : jobAssignmentList's size is now " + jobAssignmentList.size());
    		}

    	}
    }
    
    public int getSolCache() {
    	return solCache;
    }
    
    public void setSolCache(int value) {
    	solCache = value;
    }
    
    public void destroy() {
    	person = null;
    	clock  = null;
    	masterClock  = null;
        if (jobAssignmentList != null) 
        	jobAssignmentList.clear();
    }
}
