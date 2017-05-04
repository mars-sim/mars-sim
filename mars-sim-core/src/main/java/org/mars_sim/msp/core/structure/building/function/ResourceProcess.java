/**
 * Mars Simulation Project
 * ResourceProcess.java
 * @version 3.1.0 2017-05-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.resource.AmountResource;

/**
 * The ResourceProcess class represents a process of
 * converting one set of resources to another.
 */
public class ResourceProcess
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ResourceProcess.class.getName());

	/** The work time required to toggle this process on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 10D;

	private String name;
	private Map<AmountResource, Double> maxInputResourceRates;
	private Map<AmountResource, Double> maxAmbientInputResourceRates;
	private Map<AmountResource, Double> maxOutputResourceRates;
	private Map<AmountResource, Double> maxWasteOutputResourceRates;
	private boolean runningProcess;
	private double currentProductionLevel;
	private double toggleRunningWorkTime;
	private double powerRequired;

	/**
	 * Constructor.
	 * @param name the name of the process.
	 * @param powerRequired the amount of power required to run the process (kW).
	 * @param defaultOn true of process is on by default, false if off by default.
	 */
	public ResourceProcess(String name, double powerRequired, boolean defaultOn) {
		this.name = name;
		maxInputResourceRates = new HashMap<AmountResource, Double>();
		maxAmbientInputResourceRates = new HashMap<AmountResource, Double>();
		maxOutputResourceRates = new HashMap<AmountResource, Double>();
		maxWasteOutputResourceRates = new HashMap<AmountResource, Double>();
		runningProcess = defaultOn;
		currentProductionLevel = 1D;
		this.powerRequired = powerRequired;
	}

	/**
	 * Gets the process name.
	 * @return process name as string.
	 */
	public String getProcessName() {
		return name;
	}

	/**
	 * Adds a maximum input resource rate if it doesn't already exist.
	 * @param resource the amount resource.
	 * @param rate max input resource rate (kg/millisol)
	 * @param ambient is resource from available from surroundings? (air)
	 */
	public void addMaxInputResourceRate(AmountResource resource, double rate, boolean ambient) {
		if (ambient) {
			if (!maxAmbientInputResourceRates.containsKey(resource))
				maxAmbientInputResourceRates.put(resource, rate);
		}
		else {
			if (!maxInputResourceRates.containsKey(resource))
				maxInputResourceRates.put(resource, rate);
		}
	}

	/**
	 * Adds a maximum output resource rate if it doesn't already exist.
	 * @param resource the amount resource.
	 * @param rate max output resource rate (kg/millisol)
	 * @param waste is resource waste material not to be stored?
	 */
	public void addMaxOutputResourceRate(AmountResource resource, double rate, boolean waste) {
		if (waste) {
			if (!maxWasteOutputResourceRates.containsKey(resource))
				maxWasteOutputResourceRates.put(resource, rate);
		}
		else {
			if (!maxOutputResourceRates.containsKey(resource))
				maxOutputResourceRates.put(resource, rate);
		}
	}

	/**
	 * Gets the current production level of the process.
	 * @return proportion of full production (0D - 1D)
	 */
	public double getCurrentProductionLevel() {
		return currentProductionLevel;
	}

	/**
	 * Checks if the process is running or not.
	 * @return true if process is running.
	 */
	public boolean isProcessRunning() {
		return runningProcess;
	}

	/**
	 * Sets if the process is running or not.
	 * @param runningProcess true if process is running.
	 */
	public void setProcessRunning(boolean runningProcess) {
		this.runningProcess = runningProcess;
	}

	/**
	 * Adds work time to toggling the process on or off.
	 * @param time the amount (millisols) of time to add.
	 */
	public void addToggleWorkTime(double time) {
		toggleRunningWorkTime += time;
		if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
			toggleRunningWorkTime = 0D;
			runningProcess = !runningProcess;
			if (runningProcess) {
				logger.fine(name + " turned on.");
			}
			else {
				logger.fine(name + " turned off.");
			}
		}
	}

	/**
	 * Gets the set of input resources.
	 * @return set of resources.
	 */
	public Set<AmountResource> getInputResources() {
		Set<AmountResource> results = new HashSet<AmountResource>();
		results.addAll(maxInputResourceRates.keySet());
		results.addAll(maxAmbientInputResourceRates.keySet());
		return results;
	}

	/**
	 * Gets the max input resource rate for a given resource.
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputResourceRate(AmountResource resource) {
		double result = 0D;
		if (maxInputResourceRates.containsKey(resource))
			result = maxInputResourceRates.get(resource);
		else if (maxAmbientInputResourceRates.containsKey(resource))
			result = maxAmbientInputResourceRates.get(resource);
		return result;
	}

	/**
	 * Checks if resource is an ambient input.
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(AmountResource resource) {
		return maxAmbientInputResourceRates.containsKey(resource);
	}

	/**
	 * Gets the set of output resources.
	 * @return set of resources.
	 */
	public Set<AmountResource> getOutputResources() {
		Set<AmountResource> results = new HashSet<AmountResource>();
		results.addAll(maxOutputResourceRates.keySet());
		results.addAll(maxWasteOutputResourceRates.keySet());
		return results;
	}

	/**
	 * Gets the max output resource rate for a given resource.
	 * @return rate in kg/millisol.
	 */
	public double getMaxOutputResourceRate(AmountResource resource) {
		double result = 0D;
		if (maxOutputResourceRates.containsKey(resource))
			result = maxOutputResourceRates.get(resource);
		else if (maxWasteOutputResourceRates.containsKey(resource))
			result = maxWasteOutputResourceRates.get(resource);
		return result;
	}

	/**
	 * Checks if resource is a waste output.
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(AmountResource resource) {
		return maxWasteOutputResourceRates.containsKey(resource);
	}

	/**
	 * Processes resources for a given amount of time.
	 * @param time (millisols)
	 * @param productionLevel proportion of max process rate (0.0D - 1.0D)
	 * @param inventory the inventory pool to use for processes.
	 * @throws Exception if error processing resources.
	 */
	public void processResources(double time, double productionLevel,
			Inventory inventory) {

		if ((productionLevel < 0D) || (productionLevel > 1D) || (time < 0D))
			throw new IllegalArgumentException();

		// logger.info(name + " process");

		if (runningProcess) {
			// Convert time from millisols to seconds.
			// double timeSec = MarsClock.convertMillisolsToSeconds(time);

			// Get resource bottleneck
			double bottleneck = getInputBottleneck(time, inventory);
			if (productionLevel > bottleneck) productionLevel = bottleneck;

			// logger.info(name + " production level: " + productionLevel);

			// Input resources from inventory.
			Iterator<AmountResource> inputI = maxInputResourceRates.keySet().iterator();
			while (inputI.hasNext()) {
				AmountResource resource = inputI.next();
				double maxRate = maxInputResourceRates.get(resource);
				double resourceRate = maxRate * productionLevel;
				double resourceAmount = resourceRate * time;
				double remainingAmount = inventory.getAmountResourceStored(resource, false);

				// 2015-01-09 Added addDemandTotalRequest()
				inventory.addAmountDemandTotalRequest(resource);

				if (resourceAmount > remainingAmount)
					resourceAmount = remainingAmount;

				try {
					inventory.retrieveAmountResource(resource, resourceAmount);
					// 2015-01-09 addAmountDemand
					inventory.addAmountDemand(resource,resourceAmount);
				}
				catch (Exception e) {}
				// logger.info(resourceName + " input: " + resourceAmount + "kg.");
			}

			// Output resources to inventory.
			Iterator<AmountResource> outputI = maxOutputResourceRates.keySet().iterator();
			while (outputI.hasNext()) {
				AmountResource resource = outputI.next();
				double maxRate = maxOutputResourceRates.get(resource);
				double resourceRate = maxRate * productionLevel;
				double resourceAmount = resourceRate * time;
				double remainingCapacity = inventory.getAmountResourceRemainingCapacity(resource,
						false, false);
				if (resourceAmount > remainingCapacity) resourceAmount = remainingCapacity;
				try {
					inventory.storeAmountResource(resource, resourceAmount, false);
					 // 2015-01-15 Add addSupplyAmount()
		            inventory.addAmountSupplyAmount(resource, resourceAmount);
				}
				catch (Exception e) {}
				// logger.info(resourceName + " output: " + resourceAmount + "kg.");
			}
		}
		else productionLevel = 0D;

		// Set the current production level.
		currentProductionLevel = productionLevel;
	}

	/**
	 * Finds the bottleneck of input resources from inventory pool.
	 * @param time (millisols)
	 * @param inventory the inventory pool the process uses.
	 * @return bottleneck (0.0D - 1.0D)
	 * @throws Exception if error getting input bottleneck.
	 */
	private double getInputBottleneck(double time, Inventory inventory) {

		// Check for illegal argument.
		if (time < 0D) throw new IllegalArgumentException("time must be > 0D");

		double bottleneck = 1D;

		// Convert time from millisols to seconds.
		// double timeSec = MarsClock.convertMillisolsToSeconds(time);

		Iterator<AmountResource> inputI = maxInputResourceRates.keySet().iterator();
		while (inputI.hasNext()) {
			AmountResource resource = inputI.next();
			//System.out.println(resource.getName());
			double maxRate = maxInputResourceRates.get(resource);
			double desiredResourceAmount = maxRate * time;
			double inventoryResourceAmount = inventory.getAmountResourceStored(resource, false);
			double proportionAvailable = 1D;
			if (desiredResourceAmount > 0D)
				proportionAvailable = inventoryResourceAmount / desiredResourceAmount;
			if (bottleneck > proportionAvailable) bottleneck = proportionAvailable;
		}

		return bottleneck;
	}

	/**
	 * Gets the string value for this object.
	 * @return string
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the amount of power required to run the process.
	 * @return power (kW).
	 */
	public double getPowerRequired() {
		return powerRequired;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		name = null;
		//maxInputResourceRates.clear();
		maxInputResourceRates = null;
		//maxAmbientInputResourceRates.clear();
		maxAmbientInputResourceRates = null;
		//maxOutputResourceRates.clear();
		maxOutputResourceRates = null;
		//maxWasteOutputResourceRates.clear();
		maxWasteOutputResourceRates = null;
	}
}