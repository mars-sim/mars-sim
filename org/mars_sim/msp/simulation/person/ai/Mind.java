/**
 * Mars Simulation Project
 * Mind.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;

/** The Mind class represents a person's mind.
 *  It keeps track of missions and tasks which
 *  the person is involved.
 */
public class Mind implements Serializable {

    // Data members
    private Person person; // The person owning this mind.
    private MissionManager missionManager; // The simulation's mission manager.
    private TaskManager taskManager; // The person's task manager.
    private Mission mission; // The person's current mission (if any).

    /** Constructs a Mind object
     *  @param person the person owning this mind
     *  @param mars the virtual Mars
     */
    public Mind(Person person, Mars mars) {

        // Initialize data members
        this.person = person;
        missionManager = mars.getMissionManager();
        mission = null;

        // Construct a task manager
        taskManager = new TaskManager(this, mars);
    }

    /** Take appropriate action for a given amount of time
     *  @param time time in millisols
     */
    public void takeAction(double time) {
        if ((mission != null) && mission.isDone()) mission = null;
        boolean activeMission = (mission != null);

        if (taskManager.hasActiveTask()) {
	    person.getPerformanceRating();
            taskManager.performTask(time, person.getPerformanceRating());
        }
        else {
            if (activeMission) mission.performMission(person);
            if (!taskManager.hasActiveTask()) getNewAction(true, !activeMission, !activeMission);
            takeAction(time);
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
        if (mission != null) mission.removePerson(person);
        mission = newMission;
        newMission.addPerson(person);
    }

    /** Determines a new action for the person based on
     *  available tasks, missions and active missions.
     */
    public void getNewAction(boolean tasks, boolean missions, boolean activeMissions) {

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

        // Select randomly across the total weight sum.
        double rand = RandomUtil.getRandomDouble(weightSum);

        // Determine which type of action was selected and set new action accordingly.
        if (tasks) {
            if (rand < taskWeights) {
                taskManager.addTask(taskManager.getNewTask(taskWeights));
                return;
            }
            else rand -= taskWeights;
        }
        if (missions) {
            if (rand < missionWeights) {
                // System.out.println(person.getName() + " starting a new mission.");
                Mission newMission = missionManager.getNewMission(person, missionWeights);
                missionManager.addMission(newMission);
                newMission.addPerson(person);
                setMission(newMission);
                return;
            }
            else rand -= missionWeights;
        }
        if (activeMissions) {
            if (rand < activeMissionWeights) {
                // System.out.println(person.getName() + " joining a mission.");
                Mission activeMission = missionManager.getActiveMission(person, activeMissionWeights);
                if (activeMission == null) return;
                activeMission.addPerson(person);
                setMission(activeMission);
                return;
            }
            else rand -= activeMissionWeights;
        }
    }
}
