/**
 * Mars Simulation Project
 * Mind.java
 * @version 3.07 2015-03-31
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.Manager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
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

    private MasterClock masterClock;

    /** Is the job locked so another can't be chosen? */
    private boolean jobLock;

    /**
     * Constructor 1.
     * @param person the person owning this mind
     * @throws Exception if mind could not be created.
     */
    public Mind(Person person) {

        // Initialize data members
        this.person = person;
        mission = null;
        job = null;
        jobLock = false;

        // Set the MBTI personality type.
        personality = new PersonalityType(person);

        // Construct a task manager
        taskManager = new TaskManager(this);

        // Construct a skill manager.
        skillManager = new SkillManager(person);

        masterClock = Simulation.instance().getMasterClock();
    }

    /**
     * Time passing.
     * @param time the time passing (millisols)
     * @throws Exception if error.
     */
    public void timePassing(double time) {

    	// For now, a Mayor/Manager cannot switch job, no need to unlock jobLock at the end of the day
    	// The new job will be Locked in until the beginning of the next day
    	if (jobLock) {
	        MarsClock clock = masterClock.getMarsClock();
	        // check for the passing of each day
	        int solElapsed = MarsClock.getSolOfYear(clock);
	        if ( solElapsed != solCache) {
	        	solCache = solElapsed;
	        	jobLock = false;
	        }
    	}

    	// For now, a Mayor/Manager cannot switch job
    	if (job instanceof Manager)
    		jobLock = true;

    	assignJob();

        // Take action as necessary.
        takeAction(time);

        // Update stress based on personality.
        personality.updateStress(time);

        // Update relationships.
        Simulation.instance().getRelationshipManager().timePassing(person, time);

    }

    // 2015-04-30 Added assignJob() Note: first called by UnitManager at the start of the sim
    public void assignJob() {
        // Check if this person needs to get a new job or change jobs.
        if (!jobLock || job == null) {
        	// Note: getNewJob() is checking if existing job is "good enough"/ or has good prospect
         	Job newJob = JobManager.getNewJob(person);
           	// 2015-04-30 Already excluded mayor/manager job from being assigned in JobManager.getNewJob()
         	String newJobStr = newJob.getName(person.getGender());
        	String jobStr = null;
        	if (job == null)
        		jobStr = null;
        	else
        		jobStr = job.getName(person.getGender());
        	if (newJob != null) //System.out.println("timePassing() : newJob is null");
	           	if (!newJobStr.equals(jobStr)) {
		            //job = newJob;
	           		setJob(newJob, false, JobManager.SETTLEMENT);
	           	}
        	//System.out.println(person.getName() + "'s jobLock is false.");
        }
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

        if (person != null) {
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
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Could not get new action", e);
                        e.printStackTrace(System.err);
                    }
                }

                if (taskManager.hasActiveTask() || hasActiveMission()) {
                    takeAction(time);
                }
            }


        }


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

    /**
     * Sets the person's job.
     * @param newJob the new job
     * @param bypassingJobLock
     */
    public void setJob(Job newJob, boolean bypassingJobLock, String assignedBy) {
    	// if (newJob == null) System.out.println("setJob() : newJob is null");
    	// TODO : if jobLock is true, will it allow the job to be changed?
    	String newJobStr = newJob.getName(person.getGender());
    	String jobStr = null;
    	if (job == null)
    		jobStr = null;
    	else
    		jobStr = job.getName(person.getGender());
    	// TODO : check if the initiator's role allows the job to be changed
    	if (!newJobStr.equals(jobStr))
    	    if (bypassingJobLock || !jobLock) {
	            job = newJob;
		        // 2015-03-30 Added saveJob()
		        person.getJobHistory().saveJob(newJob, assignedBy);
		        person.fireUnitUpdate(UnitEventType.JOB_EVENT, newJob);
		    	// the new job will be Locked in until the beginning of the next day
		        jobLock = true;

		        int population = person.getSettlement().getAllAssociatedPeople().size();

		        // Assign a role associate with
                if (population >= UnitManager.POPULATION_WITH_MAYOR)
                	person.assignSpecialiststo7Divisions();
                else if (population >= UnitManager.POPULATION_WITH_SUB_COMMANDER)
                	person.assignSpecialiststo3Divisions();
                else
                	person.assignSpecialiststo3Divisions();

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
        	if (person != null)
                mission.removePerson(person);

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
                    mission.removePerson(person);
                }

                mission = newMission;

                if (newMission != null) {
                    newMission.addPerson(person);
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

        MissionManager missionManager = Simulation.instance().getMissionManager();

        if (person != null) {
            // If this Person is too weak then they can not do Missions
            if (person.getPerformanceRating() < 0.5D) {
                missions = false;
            }
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
	        if (person != null) {
	           missionWeights = missionManager.getTotalMissionProbability(person);
	           weightSum += missionWeights;
	        }

		}

        if (person != null) {
	        if ((weightSum <= 0D) || (Double.isNaN(weightSum)) ||
	                (Double.isInfinite(weightSum))) {
	            throw new IllegalStateException("Mind.getNewAction(): weight sum: "
	                    + weightSum);
	        }
        }



        // Select randomly across the total weight sum.
        double rand = RandomUtil.getRandomDouble(weightSum);

        // Determine which type of action was selected and set new action accordingly.
        if (tasks) {
            if (rand < taskWeights) {
                Task newTask = taskManager.getNewTask();
                taskManager.addTask(newTask);

                return;
            }
            else {
                rand -= taskWeights;
            }
        }


        if (missions) {
            if (rand < missionWeights) {
            	Mission newMission = null;
            	if (person != null) {
                    logger.fine(person.getName() + " starting a new mission.");
                    newMission = missionManager.getNewMission(person);

            	}

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

        if (person != null) {
            // If reached this point, no task or mission has been found.
            logger.severe(person.getName()
                    + " couldn't determine new action - taskWeights: "
                    + taskWeights + ", missionWeights: " + missionWeights);
        }

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