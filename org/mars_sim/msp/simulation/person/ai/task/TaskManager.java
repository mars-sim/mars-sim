/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 2.78 2005-07-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Mind;

/** 
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that person's
 * current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class TaskManager implements Serializable {

    // Data members
    private Task currentTask; // The current task the person is doing.
    private Mind mind; // The mind of the person the task manager is responsible for.

    // Array of available tasks
    private Class[] availableTasks = { Relax.class, TendGreenhouse.class,
                                       Maintenance.class, 
                                       MaintainGroundVehicleGarage.class,
                                       MaintainGroundVehicleEVA.class,
                                       Sleep.class, EatMeal.class,
                                       MedicalAssistance.class,
                                       StudyRockSamples.class,
                                       RepairMalfunction.class,
                                       RepairEVAMalfunction.class,
                                       EnterAirlock.class,
                                       Workout.class,
                                       ResearchBotany.class,
                                       ResearchMedicine.class,
                                       Teach.class, CookMeal.class,
                                       MaintenanceEVA.class };

    /** 
     * Constructor
     * @param person the person the task manager is for
     */
    public TaskManager(Mind mind) {
        // Initialize data members
        this.mind = mind;
        currentTask = null;
    }

    /** Returns true if person has an active task.
     *  @return true if person has an active task
     */
    public boolean hasActiveTask() {
        if ((currentTask != null) && !currentTask.isDone()) return true;
        else return false;
    }

    /** Returns true if perosn has a task (may be inactive).
     *  @return true if person has a task
     */
    public boolean hasTask() {
        if (currentTask != null) return true;
        else return false;
    }

    /** Returns the name of the current task for UI purposes.
     *  Returns a blank string if there is no current task.
     *  @return name of the current task
     */
    public String getTaskName() {
        if (currentTask != null) return currentTask.getName();
		else return "";
    }

    /** Returns a description of current task for UI purposes.
     *  Returns a blank string if there is no current task.
     *  @return a description of the current task
     */
    public String getTaskDescription() {
        if (currentTask != null) return currentTask.getDescription();
        else return "";
    }

    /** Returns the name of current task phase if there is one.
     *  Returns blank string if current task has no phase.
     *  Returns blank string if there is no current task.
     *  @return the name of the current task phase
     */
    public String getPhase() {
        if (currentTask != null) return currentTask.getPhase();
        else return "";
    }

    /** Returns the current task.
     *  Return null if there is no current task.
     *  @return the current task
     */
    public Task getTask() {
        return currentTask;
    }

    /**
     * Sets the current task to null.
     */
    public void clearTask() {
        currentTask = null;
    }

    /** Adds a task to the stack of tasks.
     *  @param newTask the task to be added
     */
    public void addTask(Task newTask) {
        if (hasActiveTask()) currentTask.addSubTask(newTask);
        else currentTask = newTask;
    }

    /** 
     * Perform the current task for a given amount of time.
     * @param time amount of time to perform the action
     * @param efficiency The performance rating of person performance task.
     * @throws Exception if error in performing task.
     */
    public void performTask(double time, double efficiency) throws Exception {
        if (currentTask != null) {
            // For effort driven task, reduce the effective time
            if (efficiency < .1D) efficiency = .1D; 
            if (currentTask.isEffortDriven()) time *= efficiency;
            checkForEmergency();
            
            try {
            	currentTask.performTask(time);
            }
            catch (Exception e) {
            	throw new Exception("TaskManager.performTask(): " + currentTask.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Checks if any emergencies are happening in the person's local.
     * Adds an emergency task if necessary.
     * @throws Exception if error checking for emergency.
     */
    private void checkForEmergency() throws Exception {

        // Check for emergency malfunction.
		if (RepairEmergencyMalfunction.hasEmergencyMalfunction(mind.getPerson())) {
		    boolean hasEmergencyRepair = false;
            Task task = currentTask;
	    	while (task != null) {
                if (task instanceof RepairEmergencyMalfunction) hasEmergencyRepair = true;
				task = task.getSubTask();
	    	}

	    	if (!hasEmergencyRepair) addTask(new RepairEmergencyMalfunction(mind.getPerson()));
		}
    }

    /** 
     * Gets a new task for the person based on tasks available.
     * @return new task
     * @throws Exception if new task could not be found.
     */
    public Task getNewTask() throws Exception {

        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class };
        Object[] parametersForInvokingMethod = { mind.getPerson() };

        // Get a random number from 0 to the total weight
        double totalProbability = getTotalTaskProbability(); 
        double r = RandomUtil.getRandomDouble(totalProbability);

        // Determine which task is selected.
        Class task = null;
        for (int x=0; x < availableTasks.length; x++) {
            try {
                Method probability = availableTasks[x].getMethod("getProbability", parametersForFindingMethod);
                double weight = ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();

                if (task == null) {
                    if (r < weight) task = availableTasks[x];
                    else r -= weight;
                }
            }
            catch (InvocationTargetException ie) {
                Throwable nested = ie.getTargetException();
                System.err.println("TaskManager.getNewTask() (Invocation Exception): " + nested.toString());
                System.err.println("Target = " + availableTasks[x]);
                System.err.println("Args = " + parametersForInvokingMethod);
                nested.printStackTrace();
            }
            catch (Exception e) {
            	System.err.println("TaskManager.getNewTask(): " + e.getMessage());
            }
        }

        // Construct the task
        try {
            Constructor construct = (task.getConstructor(parametersForFindingMethod));
            return (Task) construct.newInstance(parametersForInvokingMethod);
        }
        catch (Exception e) {
        	throw new Exception("TaskManager.getNewTask(): " + e.getMessage());
        }
    }

    /** Determines the total probability weight for available tasks.
     *  @return total probability weight
     */
    public double getTotalTaskProbability() {
        double result = 0D;

        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class };
        Object[] parametersForInvokingMethod = { mind.getPerson() };

        // Sum the probable weights of each available task.
        for (int x = 0; x < availableTasks.length; x++) {
            try {
                Method probability = availableTasks[x].getMethod("getProbability", parametersForFindingMethod);
                result += ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();
            } catch (Exception e) {
                System.err.println("TaskManager.getTotalTaskProbability(): " + e.toString());
            }
        }

        return result;
    }
}