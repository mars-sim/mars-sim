/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 2.90 2010-02-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.time.MarsClock;

/** 
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that person's
 * current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class TaskManager implements Serializable {

	// Unit event types
	public static final String TASK_EVENT = "task";
	
    // Data members
    private Task currentTask; // The current task the person is doing.
    private Mind mind; // The mind of the person the task manager is responsible for.

    // Array of available tasks
    private Class<? extends Task>[] availableTasks = null;
    
    // Cache variables.
    private MarsClock timeCache;
    private Map<Class<? extends Task>, Double> taskProbCache;
    private double totalProbCache;
    
    /** 
     * Constructor
     * @param mind the mind that uses this task manager.
     */
    public TaskManager(Mind mind) {
        // Initialize data members
        this.mind = mind;
        currentTask = null;
        
        // Initialize available tasks.
        availableTasks = (Class<? extends Task>[]) new Class[35];
        availableTasks[0] = Relax.class;
        availableTasks[1] = Yoga.class;
        availableTasks[2] = TendGreenhouse.class;
        availableTasks[3] = Maintenance.class;
        availableTasks[4] = MaintainGroundVehicleGarage.class;
        availableTasks[5] = MaintainGroundVehicleEVA.class;
        availableTasks[6] = Sleep.class;
        availableTasks[7] = EatMeal.class;
        availableTasks[8] = MedicalAssistance.class;
        availableTasks[9] = RepairMalfunction.class;
        availableTasks[10] = RepairEVAMalfunction.class;
        availableTasks[11] = EnterAirlock.class;
        availableTasks[12] = Workout.class;
        availableTasks[13] = Teach.class;
        availableTasks[14] = CookMeal.class;
        availableTasks[15] = MaintenanceEVA.class;
        availableTasks[16] = LoadVehicle.class;
        availableTasks[17] = UnloadVehicle.class;
        availableTasks[18] = ToggleResourceProcess.class;
        availableTasks[19] = ManufactureGood.class;
        availableTasks[20] = ToggleFuelPowerSource.class;
        availableTasks[21] = DigLocalRegolith.class;
        availableTasks[22] = PrescribeMedication.class;
        availableTasks[23] = ProposeScientificStudy.class;
        availableTasks[24] = InviteStudyCollaborator.class;
        availableTasks[25] = RespondToStudyInvitation.class;
        availableTasks[26] = PerformLaboratoryResearch.class;
        availableTasks[27] = ObserveAstronomicalObjects.class;
        availableTasks[28] = StudyFieldSamples.class;
        availableTasks[29] = PerformLaboratoryExperiment.class;
        availableTasks[30] = PerformMathematicalModeling.class;
        availableTasks[31] = CompileScientificStudyResults.class;
        availableTasks[32] = PeerReviewStudyPaper.class;
        availableTasks[33] = AssistScientificStudyResearcher.class;
        availableTasks[34] = SalvageGood.class;
        
        // Initialize cache values.
        timeCache = null;
        taskProbCache = new HashMap<Class<? extends Task>, Double>(availableTasks.length);
        totalProbCache = 0D;
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
        mind.getPerson().fireUnitUpdate(TASK_EVENT);
    }

    /** Adds a task to the stack of tasks.
     *  @param newTask the task to be added
     */
    public void addTask(Task newTask) {
        if (hasActiveTask()) currentTask.addSubTask(newTask);
        else currentTask = newTask;
        mind.getPerson().fireUnitUpdate(TASK_EVENT, newTask);
    }

    /** 
     * Perform the current task for a given amount of time.
     * @param time amount of time to perform the action
     * @param efficiency The performance rating of person performance task.
     * @return remaining time.
     * @throws Exception if error in performing task.
     */
    public double performTask(double time, double efficiency) throws Exception {
        double remainingTime = 0D;
        
        if (currentTask != null) {
            // For effort driven task, reduce the effective time based on efficiency.
            if (efficiency < .1D) efficiency = .1D; 
            if (currentTask.isEffortDriven()) time *= efficiency;
            checkForEmergency();
            
            try {
            	remainingTime = currentTask.performTask(time);
            }
            catch (Exception e) {
            	e.printStackTrace(System.err);
            	throw new Exception("TaskManager.performTask(): " + currentTask.getName() + ": " + e.getMessage());
            }
        }
        
        return remainingTime;
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

    	// If cache is not current, calculate the probabilities.
        if (!useCache()) calculateProbability();

        // Get a random number from 0 to the total weight
        double totalProbability = getTotalTaskProbability(true); 
        double r = RandomUtil.getRandomDouble(totalProbability);

        // Determine which task is selected.
        Class<? extends Task> selectedTask = null;
        Iterator<Class<? extends Task>> i = taskProbCache.keySet().iterator();
        while (i.hasNext()) {
        	Class<? extends Task> task = i.next();
        	double probWeight = ((Double) taskProbCache.get(task)).doubleValue();
        	if (selectedTask == null) {
        		if (r < probWeight) selectedTask = task;
        		else r -= probWeight;
        	}
        }

        // Construct the task
    	Class[] parametersForFindingMethod = { Person.class };
    	Object[] parametersForInvokingMethod = { mind.getPerson() };
        
        try {
            Constructor construct = selectedTask.getConstructor(parametersForFindingMethod);
            return (Task) construct.newInstance(parametersForInvokingMethod);
        }
        catch (Exception e) {
        	e.printStackTrace(System.err);
        	throw new Exception("TaskManager.getNewTask(): " + e.getMessage());
        }
    }

    /** 
     * Determines the total probability weight for available tasks.
     * @return total probability weight
     */
    public double getTotalTaskProbability(boolean useCache) {

    	// If cache is not current, calculate the probabilities.
        if (!useCache) calculateProbability();
        
        return totalProbCache;
    }
    
    /**
     * Calculates and caches the probabilities.
     */
    private void calculateProbability() {
    	// Initialize parameters.
    	Class[] parametersForFindingMethod = { Person.class };
    	Object[] parametersForInvokingMethod = { mind.getPerson() };
    	
    	// Clear total probabilities.
    	totalProbCache = 0D;
    	
    	// Determine probabilities.
    	for (int x = 0; x < availableTasks.length; x++) {
    		try {
    			Class<? extends Task> probabilityClass = availableTasks[x];
    			Method probabilityMethod = probabilityClass.getMethod("getProbability", parametersForFindingMethod);
    			Double probability = (Double) probabilityMethod.invoke(null, parametersForInvokingMethod);
    			taskProbCache.put(probabilityClass, probability);
    			totalProbCache += probability.doubleValue();
    		} 
    		catch (Exception e) {
    			e.printStackTrace(System.err);
    		}
    	}
    	
    	// Set the time cache to the current time.
    	timeCache = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    }
    
    /**
     * Checks if task probability cache should be used.
     * @return true if cache should be used.
     */
    private boolean useCache() {
    	MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    	if (currentTime.equals(timeCache)) return true;
    	return false;
    }
}