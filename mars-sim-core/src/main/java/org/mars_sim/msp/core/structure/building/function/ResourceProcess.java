/**
 * Mars Simulation Project
 * ResourceProcess.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.structure.building.ResourceProcessSpec;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ResourceProcess class represents a process of converting one set of
 * resources to another. This represent the actual process instant attached to a Building.
 */
public class ResourceProcess implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ResourceProcess.class.getName());
	
	private String name;

	private boolean runningProcess;
	private int[] timeLimit = new int[] {1, 0};
	
	private double currentProductionLevel;
	private double toggleRunningWorkTime;

	private ResourceProcessSpec definition;

	private static MarsClock marsClock;
	
	/**
	 * Constructor.
	 * 
	 * @param name          the name of the process.
	 * @param powerRequired the amount of power required to run the process (kW).
	 * @param defaultOn     true of process is on by default, false if off by
	 *                      default.
	 */
	public ResourceProcess(ResourceProcessSpec definition) {
		this.name = definition.getName();
		runningProcess = definition.getDefaultOn();
		currentProductionLevel = 1D;
		this.definition = definition;
		
		// Assume it is the start of time
		resetToggleTime(1, 0);
	}

	/**
	 * Gets the process name.
	 * 
	 * @return process name as string.
	 */
	public String getProcessName() {
		return name;
	}

	/**
	 * Gets the current production level of the process.
	 * 
	 * @return proportion of full production (0D - 1D)
	 */
	public double getCurrentProductionLevel() {
		return currentProductionLevel;
	}

	/**
	 * Checks if the process is running or not.
	 * 
	 * @return true if process is running.
	 */
	public boolean isProcessRunning() {
		return runningProcess;
	}

	/**
	 * Sets if the process is running or not.
	 * 
	 * @param runningProcess true if process is running.
	 */
	public void setProcessRunning(boolean runningProcess) {
		this.runningProcess = runningProcess;
	}

	/**
	 * Adds work time to toggling the process on or off.
	 * 
	 * @param time the amount (millisols) of time to add.
	 */
	public void addToggleWorkTime(double time) {
		toggleRunningWorkTime += time;
		if (toggleRunningWorkTime >= definition.getToggleDuration()) {
			toggleRunningWorkTime = 0D;
			runningProcess = !runningProcess;
			if (runningProcess) {
				logger.finest("Done turning on " + name);
			} else {
				logger.finest("Done turning off " + name);
			}

			// Reset for next toggle
			resetToggleTime(marsClock.getMissionSol(), marsClock.getMillisolInt());
		}
	}

	/**
	 * Gets the set of input resources.
	 * 
	 * @return set of resources.
	 */
	public Set<Integer> getInputResources() {
		return definition.getInputResources();
	}

	/**
	 * Gets the max input resource rate for a given resource.
	 * 
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputResourceRate(Integer resource) {
		return definition.getMaxInputResourceRate(resource);
	}

	/**
	 * Checks if resource is an ambient input.
	 * 
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(Integer resource) {
		return definition.isAmbientInputResource(resource);
	}
	
	/**
	 * Gets the set of output resources.
	 * 
	 * @return set of resources.
	 */
	public Set<Integer> getOutputResources() {
		return definition.getOutputResources();
	}

	/**
	 * Gets the max output resource rate for a given resource.
	 * 
	 * @return rate in kg/millisol.
	 */
	public double getMaxOutputResourceRate(Integer resource) {
		return definition.getMaxOutputResourceRate(resource);
	}

	/**
	 * Checks if resource is a waste output.
	 * 
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(Integer resource) {
		return definition.isWasteOutputResource(resource);
	}

	/**
	 * Processes resources for a given amount of time.
	 * 
	 * @param time            (millisols)
	 * @param productionLevel proportion of max process rate (0.0D - 1.0D)
	 * @param inventory       the inventory pool to use for processes.
	 * @throws Exception if error processing resources.
	 */
	public void processResources(double time, double productionLevel, Inventory inventory) {

		double level = productionLevel;
		
		if ((level < 0D) || (level > 1D) || (time < 0D))
			throw new IllegalArgumentException();


		if (runningProcess) {

			// Get resource bottleneck
			double bottleneck = getInputBottleneck(time, inventory);
			if (level > bottleneck)
				level = bottleneck;

			// logger.info(name + " production level: " + productionLevel);

			// Input resources from inventory.
			Map<Integer,Double> maxInputResourceRates = definition.getMaxInputResourceRates();
			for (Entry<Integer, Double> input : maxInputResourceRates.entrySet()) {
				Integer resource = input.getKey();
				double maxRate = input.getValue();
				double resourceRate = maxRate * level;
				double resourceAmount = resourceRate * time;
				double remainingAmount = inventory.getAmountResourceStored(resource, false);

				if (resourceAmount > remainingAmount)
					resourceAmount = remainingAmount;

				inventory.retrieveAmountResource(resource, resourceAmount);
			}

			// Output resources to inventory.
			Map<Integer,Double> maxOutputResourceRates = definition.getMaxOutputResourceRates();
			for (Entry<Integer, Double> output : maxOutputResourceRates.entrySet()) {
				Integer resource = output.getKey();
				double maxRate = output.getValue();
				double resourceRate = maxRate * level;
				double resourceAmount = resourceRate * time;
				double remainingCapacity = inventory.getAmountResourceRemainingCapacity(resource, false, false);
				if (resourceAmount > remainingCapacity)
					resourceAmount = remainingCapacity;
		
				inventory.storeAmountResource(resource, resourceAmount, false);
				inventory.addAmountSupply(resource, resourceAmount);
			}
		} else
			level = 0D;

		// Set the current production level.
		currentProductionLevel = level;
	}

	/**
	 * Finds the bottleneck of input resources from inventory pool.
	 * 
	 * @param time      (millisols)
	 * @param inventory the inventory pool the process uses.
	 * @return bottleneck (0.0D - 1.0D)
	 * @throws Exception if error getting input bottleneck.
	 */
	private double getInputBottleneck(double time, Inventory inventory) {

		// Check for illegal argument.
		if (time < 0D)
			throw new IllegalArgumentException("time must be > 0D");

		double bottleneck = 1D;
		Map<Integer,Double> maxInputResourceRates = definition.getMaxInputResourceRates();
		for (Entry<Integer, Double> input : maxInputResourceRates.entrySet()) {
			Integer resource = input.getKey();
			double maxRate = input.getValue();
			double desiredResourceAmount = maxRate * time;
			double inventoryResourceAmount = inventory.getAmountResourceStored(resource, false);
			double proportionAvailable = 1D;
			if (desiredResourceAmount > 0D)
				proportionAvailable = inventoryResourceAmount / desiredResourceAmount;
			if (bottleneck > proportionAvailable)
				bottleneck = proportionAvailable;
		}

		return bottleneck;
	}

	/**
	 * Gets the string value for this object.
	 * 
	 * @return string
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the amount of power required to run the process.
	 * 
	 * @return power (kW).
	 */
	public double getPowerRequired() {
		return definition.getPowerRequired();
	}
	
	/**
	 * Checks if it has exceeded the time limit
	 * 
	 * @return
	 */
	public boolean isToggleAvailable() {
		int sol = marsClock.getMissionSol();
		int millisol = marsClock.getMillisolInt();
		if (sol == timeLimit[0]) {
			if (millisol > timeLimit[1]) {
				return true;
			}
			else
				return false;
		}
		else {
			if (millisol > timeLimit[1] + 1000) {
				return true;
			}
			else
				return false;
		}
	}


	/**
	 * Get the time permissions for the next toggle.
	 * @return
	 */
	public int[] getTimeLimit() {
		return timeLimit;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}

	/**
	 * Reset the toggle time from teh current baseline time
	 * @param sol Baseline mission sol
	 * @param millisols
	 */
	private void resetToggleTime(int sol, int millisols) {	
		// Compute the time limit
		millisols += definition.getTogglePeriodicity();
		if (millisols >= 1000) {
			millisols = millisols - 1000;
			sol = sol + 1;
		}
		
		// Tag this particular process for toggling
		timeLimit[0] = sol;
		timeLimit[1] = millisols;
	}
}
