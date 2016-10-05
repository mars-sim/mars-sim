/**
 * Mars Simulation Project
 * Mind.java
 * @version 3.07 2015-03-31
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.Manager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The Mind class represents a person's mind. It keeps track of missions and
 * tasks which the person is involved.
 */
public class Mind
implements Serializable {

    /** default serial id.*/
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(Mind.class.getName());

    // Data members
	private int solCache = 1;
    /** The person owning this mind. */
    private Person person = null;
    /** The person's task manager. */
    private TaskManager taskManager;
    /** The person's current mission (if any). */
    private Mission mission;
    /** The person's job. */
    private Job job;
    /** The person's personality. */
    private PersonalityType personality;
    /** The person's skill manager. */
    private SkillManager skillManager;

    private MissionManager missionManager;
    /** Is the job locked so another can't be chosen? */
    private boolean jobLock;

    private static Simulation sim = Simulation.instance();
    //private static MasterClock masterClock;// = sim.getMasterClock();
    //private static MarsClock marsClock;// = masterClock.getMarsClock();
    
    /**
     * Constructor 1.
     * @param person the person owning this mind
     * @throws Exception if mind could not be created.
     */
    public Mind(Person person) {
		//logger.info("Mind's constructor is in " + Thread.currentThread().getName() + " Thread");

        // Initialize data members
        this.person = person;
        mission = null;
        job = null;
        jobLock = false;

        sim = Simulation.instance();
        //masterClock = sim.getMasterClock();
        //if (masterClock != null) { // to avoid NullPointerException during maven test
	    //    marsClock = masterClock.getMarsClock();
        //}
        // Set the MBTI personality type.
        personality = new PersonalityType(person);

        // Construct a task manager
        taskManager = new TaskManager(this);

        missionManager = sim.getMissionManager();

        // Construct a skill manager.
        skillManager = new SkillManager(person);

        //masterClock = sim.getMasterClock();
    }

    /**
     * Time passing.
     * @param time the time passing (millisols)
     * @throws Exception if error.
     */
    public void timePassing(double time) {
		//logger.info("Mind's timePassing() is in " + Thread.currentThread().getName() + " Thread");

        if (taskManager != null)
        	// 2015-10-22 Added recordTask()
    		taskManager.recordTask();

        if (missionManager != null)
        	// 2015-10-31 Added recordMission()
        	missionManager.recordMission();

    	// Note : for now a Mayor/Manager cannot switch job
    	if (job instanceof Manager)
    		jobLock = true;

     	else {
    		if (jobLock) {
    	        //if (masterClock == null)
    	        //	masterClock = sim.getMasterClock();
    	        
    			//if (marsClock == null)
    			//	marsClock = masterClock.getMarsClock();// needed for loading a saved sim 

    		   	// Note: for non-manager, the new job will be locked in until the beginning of the next day
    	        // check for the passing of each day
    	        int solElapsed = sim.getMasterClock().getMarsClock().getSolElapsedFromStart();
    	        if (solElapsed != solCache) {
    	        	solCache = solElapsed;
    	        	jobLock = false;
    	        }
        	}
    		else
    			checkJob();
    	}
    	

        // Update stress based on personality.
        personality.updateStress(time);

        // Update relationships.
        sim.getRelationshipManager().timePassing(person, time);

        // Take action as necessary.
        if (taskManager != null)
        	takeAction(time);

    }

    /*
     * Checks if a person has a job. If not, get a new one.
     */
    // 2015-04-30 Added checkJob()
    public void checkJob() { //String status, String approvedBy) {
        // Check if this person needs to get a new job or change jobs.
        if (job == null) { // removing !jobLock 
        	// Note: getNewJob() is checking if existing job is "good enough"/ or has good prospect
         	Job newJob = JobManager.getNewJob(person);
           	// 2015-04-30 Already excluded mayor/manager job from being assigned in JobManager.getNewJob()
         	String newJobStr = newJob.getName(person.getGender());
        	String jobStr = null;
        	if (job != null)
        		jobStr = job.getName(person.getGender());
        	if (newJob != null) {
        		//System.out.println("timePassing() : newJob is null"); 	
	           	if (!newJobStr.equals(jobStr)) {
		            //job = newJob;
	           		setJob(newJob, false, JobManager.SETTLEMENT, JobAssignmentType.APPROVED, JobManager.SETTLEMENT);
	           	}
        	}
        	//System.out.println(person.getName() + "'s jobLock is false.");
        }
    }

    //  first called by UnitManager at the start of the sim
    public void getInitialJob(String assignedBy) {
    	Job newJob = JobManager.getNewJob(person);
    	setJob(newJob, true, assignedBy, JobAssignmentType.APPROVED, assignedBy);
    }

    /**
     * Take appropriate action for a given amount of time.
     * @param time time in millisols
     * @throws Exception if error during action.
     */
    public void takeAction(double time) {

        if ((mission != null) && mission.isDone()) {
            mission = null;
        }

        boolean activeMission = (mission != null);

        // Check if mission creation at settlement (if any) is overridden.
        boolean overrideMission = false;

        //if (person != null) {
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                overrideMission = person.getSettlement().getMissionCreationOverride();
            }

            // Perform a task if the person has one, or determine a new task/mission.
            if (taskManager.hasActiveTask()) {
                double remainingTime = taskManager.performTask(time, person
                        .getPerformanceRating());
                if (remainingTime > 0D) {
                    takeAction(remainingTime);
                }
            }
            else {
                if (activeMission) {
                    mission.performMission(person);
                }

                if (!taskManager.hasActiveTask()) {
                    try {
                        getNewAction(true, (!activeMission && !overrideMission));
                        // 2015-10-22 Added recordTask()
                        //taskManager.recordTask();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, person + " could not get new action", e);
                        e.printStackTrace(System.err);
                    }
                }

                if (taskManager.hasActiveTask() || hasActiveMission()) {
                    takeAction(time);
                }
            }


       // }


    }


    /**
     * Reassign the person's job.
     * @param newJob the new job
     * @param bypassingJobLock
     */
    // 2015-09-23 Renamed to reassignedJob()
    // Called by
    // (1) ReviewJobReassignment's constructor or
    // (2) TabPanelCareer's actionPerformed() [for a pop <= 4 settlement]
    public void reassignJob(String newJobStr, boolean bypassingJobLock, String assignedBy, JobAssignmentType status, String approvedBy) {
    	//System.out.println("\n< " + person.getName() + " > ");
	    //System.out.println("Mind.java : reassignJob() : starting ");
    	Job newJob = null;
    	Iterator<Job> i = JobManager.getJobs().iterator();
	    while (i.hasNext()) {
		    Job job = i.next();
		    String n = job.getName(person.getGender());
			if (newJobStr.equals(n))
				// gets selectedJob by running through iterator to match it
				newJob = job;
		}

    	assignJob(newJob, newJobStr, bypassingJobLock, assignedBy, status, approvedBy);
    }


    /**
     * Sets the person's job.
     * @param newJob the new job
     * @param bypassingJobLock
     * @param assignedBy
     * @param status of JobAssignmentType
     * @param approvedBy
     */
    // Called by
    // (1) setRole() in Person.java (if a person has a Manager role type)
    // (2) checkJob() in Mind.java
    // (3) getInitialJob() in Mind.java
    public void setJob(Job newJob, boolean bypassingJobLock, String assignedBy, JobAssignmentType status, String approvedBy) {
    	//System.out.println("\n< " + person.getName() + " > ");
    	//System.out.println("Mind.java : setJob() : starting");
    	// if (newJob == null) System.out.println("setJob() : newJob is null");
    	// TODO : if jobLock is true, will it allow the job to be changed?
    	String newJobStr = newJob.getName(person.getGender());

	    //System.out.println("Mind.java : setJob() : calling assignJob() ");
    	assignJob(newJob, newJobStr, bypassingJobLock, assignedBy, status, approvedBy);
    }

    /**
     * Assigns a person a new job.
     * @param newJob the new job
     * @param bypassingJobLock
     * @param assignedBy
     * @param status of JobAssignmentType
     * @param approvedBy
     */
    public void assignJob(Job newJob, String newJobStr, boolean bypassingJobLock, String assignedBy, JobAssignmentType status, String approvedBy) {
    	//System.out.println("Mind.java : assignJob() : starting");
    	String jobStr = null;
    	if (job == null)
    		jobStr = null;
    	else
    		jobStr = job.getName(person.getGender());

    	//System.out.println("Mind.java : assignJob() : old jobStr was " + jobStr);
    	//System.out.println("Mind.java : assignJob() : old job was " + job);
       	//System.out.println("Mind.java : assignJob() : bypassingJobLock = " + bypassingJobLock
       	//		+ "  jobLock = " + jobLock);

    	// TODO : check if the initiator's role allows the job to be changed
    	if (!newJobStr.equals(jobStr)) {
    		
    	    if (bypassingJobLock || !jobLock) {
	            job = newJob;

	            //System.out.println("Mind.java : assignJob(): approvedBy is " + approvedBy);
		        // 2015-09-23 Set up 4 approvedBy conditions
		        if (approvedBy.equals(JobManager.SETTLEMENT)) { // automatically approved if pop <= 4
		        	//System.out.println("Mind.java : assignJob() : pop > 4, calling JobHistory's saveJob(), approved by Settlement");
		        	person.getJobHistory().saveJob(newJob, assignedBy, status, approvedBy, true);
		        }
		        else if (approvedBy.equals(JobManager.USER)) {
		        // if (person.getAssociatedSettlement().getAllAssociatedPeople().size() <= 4) {
	        		//System.out.println("Mind.java : assignJob() : pop <= 4, calling JobHistory's saveJob(), approved by User");
	        		person.getJobHistory().saveJob(newJob, assignedBy, status, approvedBy, true);
		        }
		        else if (approvedBy.equals(JobManager.MISSION_CONTROL)) { // at the start of sim
	            	//System.out.println("Mind.java : assignJob() : calling JobHistory's saveJob(), approved by Mission Control");
	        		person.getJobHistory().saveJob(newJob, assignedBy, status, approvedBy, false);
		        }
		        else { // if approvedBy = name of commander/subcommander/mayor/president
	        		//System.out.println("Mind.java : assignJob() : calling JobHistory's saveJob(), approved by a Senior Official");
	        		person.getJobHistory().saveJob(newJob, assignedBy, status, approvedBy, false);
		        }

		    	//System.out.println("just called JobHistory's saveJob()");
		        person.fireUnitUpdate(UnitEventType.JOB_EVENT, newJob);

		        int population = 0;

		        // Assign a new role type to the person and others in a settlement
		        // immediately after the change of one's job type
		        if (person.getSettlement() != null) {
		        	ChainOfCommand cc = person.getSettlement().getChainOfCommand();
		        	population = person.getSettlement().getAllAssociatedPeople().size();
			        // Assign a role associate with
	                if (population >= UnitManager.POPULATION_WITH_MAYOR) {
	                	cc.set7Divisions(true);
	                	cc.assignSpecialiststo7Divisions(person);
	                }
	                //else if (population >= UnitManager.POPULATION_WITH_SUB_COMMANDER) {
	                //	person.getSettlement().set3Divisions(true);
	                //	UnitManager.assignSpecialiststo3Divisions(person);
	                //}
	                else {
	                	cc.set3Divisions(true);
	                	cc.assignSpecialiststo3Divisions(person);
	                }

		        }
		        
		    	// the new job will be Locked in until the beginning of the next day
		        jobLock = true;
		        //System.out.println("Mind's assignJob() : just set jobLock = true");
    	    }
    	}
    }

    /**
     * Returns true if person has an active mission.
     * @return true for active mission
     */
    public boolean hasActiveMission() {
        return (mission != null) && !mission.isDone();
    }

    /**
     * Set this mind as inactive. Needs move work on this; has to abort the Task can not just close it. This abort
     * action would then allow the Mission to be also aborted.
     */
    public void setInactive() {
        taskManager.clearTask();
        if (hasActiveMission()) {
        	//if (person != null) {
                mission.removeMember(person);
        	//}

            mission = null;
        }
    }

    /**
     * Sets the person's current mission.
     * @param newMission the new mission
     */
    public void setMission(Mission newMission) {
        if (newMission != mission) {

        	if (person != null) {
        		if (mission != null) {
                    mission.removeMember(person);
                }

                mission = newMission;

                if (newMission != null) {
                    newMission.addMember(person);
                }

                person.fireUnitUpdate(UnitEventType.MISSION_EVENT, newMission);
        	}

        }
    }

    /**
     * Determines a new action for the person based on available tasks, missions and active missions.
     * @param tasks can actions be tasks?
     * @param missions can actions be new missions?
     */
    public void getNewAction(boolean tasks, boolean missions) {

    	// If this Person is too weak then they can not do Missions
    	if (person.getPerformanceRating() < 0.5D) {
    		missions = false;
    	}


        // Get probability weights from tasks, missions and active missions.
        double taskWeights = 0D;
        double missionWeights = 0D;

        // Determine sum of weights based on given parameters
        double weightSum = 0D;

        if (tasks) {
            taskWeights = taskManager.getTotalTaskProbability(false);
            weightSum += taskWeights;
        }

        if (missions) {
	           missionWeights = missionManager.getTotalMissionProbability(person);
	           weightSum += missionWeights;
		}

        if ((weightSum <= 0D) || (Double.isNaN(weightSum)) || (Double.isInfinite(weightSum))) {
        	try {
				TimeUnit.MILLISECONDS.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            throw new IllegalStateException("Mind.getNewAction(): " + person + " weight sum: "
                    + weightSum);
        }

        // Select randomly across the total weight sum.
        double rand = RandomUtil.getRandomDouble(weightSum);

        // Determine which type of action was selected and set new action accordingly.
        if (tasks) {
            if (rand < taskWeights) {
                Task newTask = taskManager.getNewTask();
                if (newTask != null)
                	taskManager.addTask(newTask);
                else 
                	logger.severe(person + " : newTask is null ");                

                return;
            }
            else {
                rand -= taskWeights;
            }
        }


        if (missions) {
            if (rand < missionWeights) {
            	Mission newMission = null;
            	logger.fine(person.getName() + " starting a new mission.");
            	newMission = missionManager.getNewMission(person);

                if (newMission != null) {
                    missionManager.addMission(newMission);
                    setMission(newMission);
                }

                return;
            }
            else {
                rand -= missionWeights;
            }
        }

        // If reached this point, no task or mission has been found.
        logger.severe(person.getName()
                    + " couldn't determine new action - taskWeights: "
                    + taskWeights + ", missionWeights: " + missionWeights);
    }

    /**
     * Gets the person's personality type.
     * @return personality type.
     */
    public PersonalityType getPersonalityType() {
        return personality;
    }

    /**
     * Returns a reference to the Person's skill manager
     * @return the person's skill manager
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }


    /**
     * Returns the person owning this mind.
     * @return person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Returns the person's task manager
     * @return task manager
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * Returns the person's current mission. Returns null if there is no current mission.
     * @return current mission
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Gets the person's job
     * @return job or null if none.
     */
    public Job getJob() {
        return job;
    }

     /**
     * Checks if the person's job is locked and can't be changed.
     * @return true if job lock.
     */
    public boolean getJobLock() {
        return jobLock;
    }

    /*
     * Set the value of jobLock so that the job can or cannot be changed
     */
    public void setJobLock(boolean value) {
    	jobLock = value;
    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        person = null;
        taskManager.destroy();
        if (mission != null) mission.destroy();
        mission = null;
        job = null;
        if (personality !=null) personality.destroy();
        personality = null;
        skillManager.destroy();
        skillManager = null;
    }
}