/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 3.07 2015-01-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTaskUtil;
import org.mars_sim.msp.core.time.MarsClock;

/** 
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that person's
 * current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class TaskManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TaskManager.class.getName());

	// Data members
	/** The current task the person is doing. */
	private Task currentTask;
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;

	// Cache variables.
	private transient MarsClock timeCache;
	private transient double totalProbCache;
	private transient Map<MetaTask, Double> taskProbCache;

	private Person person = null;
	private Robot robot = null;
	
	/** 
	 * Constructor.
	 * @param mind the mind that uses this task manager.
	 */
	public TaskManager(Mind mind) {
		// Initialize data members
		this.mind = mind;
		
		this.person = mind.getPerson();
		this.robot = mind.getRobot();
		
		currentTask = null;

		// Initialize cache values.
		timeCache = null;
		taskProbCache = new HashMap<MetaTask, Double>(MetaTaskUtil.getMetaTasks().size());
		totalProbCache = 0D;
	}

	/**
	 * Returns true if person has an active task.
	 * @return true if person has an active task
	 */
	public boolean hasActiveTask() {
		return (currentTask != null) && !currentTask.isDone();
	}

	/**
	 * Returns true if person has a task (may be inactive).
	 * @return true if person has a task
	 */
	public boolean hasTask() {
		return currentTask != null;
	}

	/**
	 * Returns the name of the current task for UI purposes.
	 * Returns a blank string if there is no current task.
	 * @return name of the current task
	 */
	public String getTaskName() {
		if (currentTask != null) {
			return currentTask.getName();
		} else {
			return "";
		}
	}

	/**
	 * Returns a description of current task for UI purposes.
	 * Returns a blank string if there is no current task.
	 * @return a description of the current task
	 */
	public String getTaskDescription() {
		if (currentTask != null) {
			return currentTask.getDescription();
		} else {
			return "";
		}
	}

	/**
	 * Returns the current task phase if there is one.
	 * Returns null if current task has no phase.
	 * Returns null if there is no current task.
	 * @return the current task phase
	 */
	public TaskPhase getPhase() {
		if (currentTask != null) {
			return currentTask.getPhase();
		} else {
			return null;
		}
	}

	/**
	 * Returns the current task.
	 * Return null if there is no current task.
	 * @return the current task
	 */
	public Task getTask() {
		return currentTask;
	}

	/**
	 * Sets the current task to null.
	 */
	public void clearTask() {
		currentTask.endTask();
		currentTask = null;
		
		if (person != null) 
			person.fireUnitUpdate(UnitEventType.TASK_EVENT);
		else if (robot != null)
			robot.fireUnitUpdate(UnitEventType.TASK_EVENT);		
	}

	/**
	 * Adds a task to the stack of tasks.
	 * @param newTask the task to be added
	 */
	public void addTask(Task newTask) {
		if (hasActiveTask()) {
			currentTask.addSubTask(newTask);
		} else {
			currentTask = newTask;
		}
			
		if (person != null) 
			person.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
		else if (robot != null)
			robot.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);		
		
	}

    public void reduceEnergy(double time) {
    	
		// 2015-01-30 Added reducekJoules()
		// TODO: need to match the kind of activity to the energy output
		PhysicalCondition health = person.getPhysicalCondition();
		int ACTIVITY_FACTOR = 6;
		double newTime = ACTIVITY_FACTOR * time ;
		health.reduceEnergy(newTime);
        //System.out.println("TaskManager : reduce Energy by "+ Math.round( newTime * 10.0)/10.0);
		
    }
    
	/** 
	 * Perform the current task for a given amount of time.
	 * @param time amount of time to perform the action
	 * @param efficiency The performance rating of person performance task.
	 * @return remaining time.
	 * @throws Exception if error in performing task.
	 */
	public double performTask(double time, double efficiency) {
		double remainingTime = 0D;
		if (currentTask != null) {
			// For effort driven task, reduce the effective time based on efficiency.
			if (efficiency < .1D) {
				efficiency = .1D;
			}
			if (currentTask.isEffortDriven()) {
				time *= efficiency;
			}
			
			checkForEmergency();
			remainingTime = currentTask.performTask(time);
		}
		
        reduceEnergy(time - remainingTime);
		
        
		return remainingTime;
		
	}

	private boolean doingEmergencyRepair() {

	    // Check if person is already repairing an emergency.
	    boolean hasEmergencyRepair = ((currentTask != null) && (currentTask 
				instanceof RepairEmergencyMalfunction));
		if (((currentTask != null) && (currentTask instanceof RepairEmergencyMalfunctionEVA))) {
		    hasEmergencyRepair = true;
		}
		return hasEmergencyRepair;
	}
	
	private boolean doingAirlockTask() {
		// Check if robot is performing an airlock task.
		boolean hasAirlockTask = false;
		Task task = currentTask;
		while (task != null) {
			if ((task instanceof EnterAirlock) || (task instanceof ExitAirlock)) {
				hasAirlockTask = true;
			}
			task = task.getSubTask();
		}
		
		return hasAirlockTask;
	}
	
	/**
	 * Checks if any emergencies are happening in the person's local.
	 * Adds an emergency task if necessary.
	 * @throws Exception if error checking for emergency.
	 */
	private void checkForEmergency() {
	    
		if (person != null) {
			
			// Check for emergency malfunction.
			if (RepairEmergencyMalfunction.hasEmergencyMalfunction(person)) {
				
			    // Check if person is already repairing an emergency.
			    boolean hasEmergencyRepair = doingEmergencyRepair();
				
				// Check if person is performing an airlock task.
				boolean hasAirlockTask = doingAirlockTask();
				
				// Check if person is outside.
				boolean isOutside = person.getLocationSituation() == LocationSituation.OUTSIDE;								
				
				// Cancel current task and start emergency repair task.
				if (!hasEmergencyRepair && !hasAirlockTask && !isOutside) {
					
					if (RepairEmergencyMalfunctionEVA.requiresEVARepair(person)) {
			            
			            if (RepairEmergencyMalfunctionEVA.canPerformEVA(person)) {
			                
			                logger.fine(person + " cancelling task " + currentTask + 
			                        " due to emergency EVA repairs.");
			                clearTask();
			                addTask(new RepairEmergencyMalfunctionEVA(person));
			            }
					}
					else {
					    logger.fine(person + " cancelling task " + currentTask + 
		                        " due to emergency repairs.");
		                clearTask();
					    addTask(new RepairEmergencyMalfunction(person));
					}
				}
			}
		}
		else if (robot != null) {
			
			// Check for emergency malfunction.
			if (RepairEmergencyMalfunction.hasEmergencyMalfunction(robot)) {
				
			    // Check if robot is already repairing an emergency.
			    boolean hasEmergencyRepair = doingEmergencyRepair();
				
				// Check if robot is performing an airlock task.
				boolean hasAirlockTask = doingAirlockTask();
				
				// Check if robot is outside.
				boolean isOutside = robot.getLocationSituation() == LocationSituation.OUTSIDE;
				
				// Cancel current task and start emergency repair task.
				if (!hasEmergencyRepair && !hasAirlockTask && !isOutside) {
					
					if (RepairEmergencyMalfunctionEVA.requiresEVARepair(robot)) {
			            
			            if (RepairEmergencyMalfunctionEVA.canPerformEVA(robot)) {
			                
			                logger.fine(robot + " cancelling task " + currentTask + 
			                        " due to emergency EVA repairs.");
			                clearTask();
			                addTask(new RepairEmergencyMalfunctionEVA(robot));
			            }
					}
					else {
					    logger.fine(robot + " cancelling task " + currentTask + 
		                        " due to emergency repairs.");
		                clearTask();
					    addTask(new RepairEmergencyMalfunction(robot));
					}
				}
			}
		}

	}

	/** 
	 * Gets a new task for the person based on tasks available.
	 * @return new task
	 */
	public Task getNewTask() {
		Task result = null;
		// If cache is not current, calculate the probabilities.
		if (!useCache()) {
			calculateProbability();
		}
		// Get a random number from 0 to the total weight
		double totalProbability = getTotalTaskProbability(true);
				
		if (totalProbability == 0D) {
			
			if (person != null) {
				throw new IllegalStateException(mind.getPerson() + 
						" has zero total task probability weight.");
			}
			else if (robot != null) {
				throw new IllegalStateException(mind.getRobot() + 
						" has zero total task probability weight.");			
			}	
		}
		
		
		double r = RandomUtil.getRandomDouble(totalProbability);
		// Determine which task is selected.
		MetaTask selectedMetaTask = null;
		Iterator<MetaTask> i = taskProbCache.keySet().iterator();
		while (i.hasNext() && (selectedMetaTask == null)) {
			MetaTask metaTask = i.next();
			double probWeight = taskProbCache.get(metaTask);
			if (r <= probWeight) {
				selectedMetaTask = metaTask;
			} 
			else {
				r -= probWeight;
			}
		}
		if (selectedMetaTask == null) {
			if (person != null) 
				throw new IllegalStateException(mind.getPerson() + 
						" could not determine a new task.");
			else if (robot != null)
					throw new IllegalStateException(mind.getRobot() + 
							" could not determine a new task.");
			
		}
		if (person != null) {
			// Construct the task
			result = selectedMetaTask.constructInstance(mind.getPerson());
		}
		else if (robot != null) {
			// Construct the task
			result = selectedMetaTask.constructInstance(mind.getRobot());
		}

		
		// Clear time cache.
		timeCache = null;
		return result;
	}

	/** 
	 * Determines the total probability weight for available tasks.
	 * @return total probability weight
	 */
	public double getTotalTaskProbability(boolean useCache) {
		// If cache is not current, calculate the probabilities.
		if (!useCache) {
			calculateProbability();
		}
		return totalProbCache;
	}

	/**
	 * Calculates and caches the probabilities.
	 */
	private void calculateProbability() {
		if (taskProbCache == null) {
			taskProbCache = new HashMap<MetaTask, Double>(MetaTaskUtil.getMetaTasks().size());
		}
		// Clear total probabilities.
		totalProbCache = 0D;
		// Determine probabilities.
		Iterator<MetaTask> i = MetaTaskUtil.getMetaTasks().iterator();
		
		while (i.hasNext()) {
			MetaTask metaTask = i.next();
			double probability = 0;
			Person person = mind.getPerson();
			Robot robot = mind.getRobot();
			
			if (person != null) {
				probability = metaTask.getProbability(person);
			}
			else if (robot != null) {
				probability = metaTask.getProbability(robot);
			}
			
			
			if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				taskProbCache.put(metaTask, probability);
				totalProbCache += probability;
			}
			else {
				taskProbCache.put(metaTask, 0D);
				
				if (person != null) {
					logger.severe(mind.getPerson().getName() + " bad task probability: " +  metaTask.getName() + 
							" probability: " + probability);
				}
				else if (robot != null) {
					logger.severe(mind.getRobot().getName() + " bad task probability: " +  metaTask.getName() + 
							" probability: " + probability);
				}
				
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
		return currentTime.equals(timeCache);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (currentTask != null) {
			currentTask.destroy();
		}
		mind = null;
		person = null;
		robot = null;
		timeCache = null;
		if (taskProbCache != null) {
			taskProbCache.clear();
			taskProbCache = null;
		}
	}
}