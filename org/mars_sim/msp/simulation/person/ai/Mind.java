/**
 * Mars Simulation Project
 * Mind.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.job.*;
import org.mars_sim.msp.simulation.person.ai.mission.*;
import org.mars_sim.msp.simulation.person.ai.task.*;

/** The Mind class represents a person's mind.
 *  It keeps track of missions and tasks which
 *  the person is involved.
 */
public class Mind implements Serializable {

    // Data members
    private Person person; // The person owning this mind.
    private TaskManager taskManager; // The person's task manager.
    private Mission mission; // The person's current mission (if any).
    private Job job; // The person's job.

    /** 
     * Constructor
     * @param person the person owning this mind
     */
    public Mind(Person person) {

        // Initialize data members
        this.person = person;
        mission = null;
        job = null;

        // Construct a task manager
        taskManager = new TaskManager(this);
    }

    /** 
     * Take appropriate action for a given amount of time.
     * @param time time in millisols
     * @throws Exception if error during action.
     */
    public void takeAction(double time) throws Exception {
        
        if ((mission != null) && mission.isDone()) mission = null;
        
        boolean activeMission = (mission != null);
        
        try {
        	if (taskManager.hasActiveTask()) {
            	taskManager.performTask(time, person.getPerformanceRating());
        	}
        	else {
            	if (activeMission) mission.performMission(person);
            	if (!taskManager.hasActiveTask()) getNewAction(true, !activeMission, !activeMission);
            	takeAction(time);
        	}
        }
        catch (Exception e) {
        	throw new Exception("Mind.takeAction(): " + e.getMessage());
        }
    }

    /** Returns the person owning this mind.
     *  @return person
     */
    public Person getPerson() {
        return person;
    }

    /** Returns the person's task manager
     *  @return task manager
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /** Returns the person's current mission.
     *  Returns null if there is no current mission.
     *  @return current mission
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

    /** Returns true if person has an active mission.
     *  @return true for active mission
     */
    public boolean hasActiveMission() {
        if ((mission != null) && !mission.isDone()) return true;
        else return false;
    }

    /**
     * Set this mind as inactive. Needs move work on this; has to abort the Task
     * can not just close it. This abort action would then allow the Mission to
     * be also aborted.
     */
    public void setInactive() {
	taskManager.clearTask();
        if (hasActiveMission()) {
            mission.removePerson(person);
            mission = null;
        }
    }

    /** Sets the person's current mission.
     *  @param newMission the new mission
     */
    public void setMission(Mission newMission) {
        if (mission != null) {
            mission.removePerson(person);
        }
        mission = newMission;
        newMission.addPerson(person);
    }
    
    /**
     * Checks to see if the person should change his/her job.
     * (Note: more work here needed)
     * @return true if change job.
     */
    private boolean shouldChangeJob() {
    	boolean result = false;
    	return result;
    }

    /** 
     * Determines a new action for the person based on
     * available tasks, missions and active missions.
     * @param tasks can actions be tasks?
     * @param missions can actions be new missions?
     * @param activeMissions can actions be active missions?
     * @throws Exception if new action cannot be found.
     */
    public void getNewAction(boolean tasks, boolean missions, boolean activeMissions) throws Exception {

		MissionManager missionManager = Simulation.instance().getMissionManager();
		JobManager jobManager = Simulation.instance().getJobManager();
		
		// Check if this person needs to get a new job or change jobs.
		if ((job == null) || shouldChangeJob()) {
			job = jobManager.getNewJob(person);
			// System.out.println(person.getName() + " job is " + job.getName());
		} 

        // If this Person is too weak then they can not do Missions
        if (person.getPerformanceRating() < 0.5D) {
            missions = false;
            activeMissions = false;
        }

        // Get probability weights from tasks, missions and active missions.
        double taskWeights = taskManager.getTotalTaskProbability();
        double missionWeights = missionManager.getTotalMissionProbability(person);
        double activeMissionWeights = missionManager.getTotalActiveMissionProbability(person);

        // Determine sum of weights based on given parameters
        double weightSum = 0D;
        if (tasks) weightSum += taskWeights;
        if (missions) weightSum += missionWeights;
        if (activeMissions) weightSum += activeMissionWeights;
		if (weightSum <= 0D) throw new Exception("Mind.getNewAction(): weight sum: " + weightSum);

        // Select randomly across the total weight sum.
        double rand = RandomUtil.getRandomDouble(weightSum);

        // Determine which type of action was selected and set new action accordingly.
        if (tasks) {
            if (rand < taskWeights) {
                taskManager.addTask(taskManager.getNewTask());
                return;
            }
            else rand -= taskWeights;
        }
        if (missions) {
            if (rand < missionWeights) {
                // System.out.println(person.getName() + " starting a new mission.");
                Mission newMission = missionManager.getNewMission(person);
                missionManager.addMission(newMission);

                setMission(newMission);
                return;
            }
            else rand -= missionWeights;
        }
        if (activeMissions) {
            if (rand < activeMissionWeights) {
                // System.out.println(person.getName() + " joining a mission.");
                Mission activeMission = missionManager.getActiveMission(person);
                if (activeMission == null) return;
                activeMission.addPerson(person);
                setMission(activeMission);
                return;
            }
            else rand -= activeMissionWeights;
        }
    }
}