/*
 * Mars Simulation Project
 * ResourceProcess.java
 * @date 2022-07-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.ResourceProcessSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ResourceProcess class represents a process of converting one set of
 * resources to another. This represent the actual process instant attached to a Building.
 */
public class ResourceProcess implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResourceProcess.class.getName());

	private static final double SMALL_AMOUNT = 0.000001;

	// How often should the process be checked? 
	private static final double PROCESS_CHECK_FREQUENCY = 5D; // 200 times per sol
	
	/** Flag to toggle on or off. */
	private boolean toToggleOn;
	/** Flag for change. */
	private boolean flag = false;
	/** is this process running ? */
	private boolean runningProcess;
	/** The time accumulated [in millisols]. */
	private double accumulatedTime;

	private double currentProductionLevel;
	
	private double toggleRunningWorkTime;

	private String name;

	private int[] timeLimit = new int[] {1, 0};

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
	 * Checks if the process has been flagged for change.
	 *
	 * @return true if the process has been flagged for change.
	 */
	public boolean isFlagged() {
		return flag;
	}

	/**
	 * Flags the process for change.
	 *
	 * @param value true if the flag is true.
	 */
	public void setFlag(boolean value) {
		flag = value;
	}
	
	/**
	 * Sets the process to be toggled on or off.
	 *
	 * @param value
	 */
	public void setToggleOn(boolean value) {
		toToggleOn = value;
	}
	

	/**
	 * Checks if the process has been flagged to be toggled on or off.
	 *
	 * @return true if it was flagged to be toggled on. False if it was flagged to be off.
	 */
	public boolean isToggledOn() {
		return toToggleOn;
	}
	
	/**
	 * Adds work time to toggling the process on or off.
	 *
	 * @param time the amount (millisols) of time to add.
	 * @return true if done
	 */
	public boolean addToggleWorkTime(double time) {
		toggleRunningWorkTime += time;
		if (toggleRunningWorkTime >= definition.getToggleDuration()) {
			toggleRunningWorkTime = 0D;
			
			runningProcess = !runningProcess;

			// Reset for next toggle
			resetToggleTime(marsClock.getMissionSol(), marsClock.getMillisolInt());
			
			return true;
		}
		
		return false;
	}

	public double getRemainingToggleWorkTime() {
		double time = definition.getToggleDuration() - toggleRunningWorkTime;
		if (time > 0)
			return time;
		else
			return 0;
	}
			
	/**
	 * Gets the set of input resources.
	 *
	 * @return set of resources.
	 */
	public Set<Integer> getInputResources() {
		return definition.getInputResources();
	}

	public int getNumModules() {
		return definition.getNumModules();
	}
	
	/**
	 * Gets the max input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputRate(Integer resource) {
		return definition.getMaxInputRate(resource);
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

	public boolean isInSitu(int resource) {
		return ResourceUtil.isInSitu(resource);
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
	public double getMaxOutputRate(Integer resource) {
		return definition.getMaxOutputRate(resource);
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
	 * @param pulse
	 * @param productionLevel proportion of max process rate (0.0D - 1.0D)
	 * @param inventory       the inventory pool to use for processes.
	 * @throws Exception if error processing resources.
	 */
	public void processResources(ClockPulse pulse, double productionLevel, Settlement settlement) {
		double time = pulse.getElapsed();
		double level = productionLevel;

		if ((level < 0D) || (level > 1D) || (time < SMALL_AMOUNT))
			return;

		if (runningProcess) {

			accumulatedTime += time;

			if (accumulatedTime >= PROCESS_CHECK_FREQUENCY) {
//				logger.info(settlement, 30_000, name + "  pulse width: " + Math.round(time * 10000.0)/10000.0 
//						+ "  accumulatedTime: " + Math.round(accumulatedTime * 100.0)/100.0 
//						+ "  processInterval: " + processInterval);
	
				double bottleneck = 1D;

				// Input resources from inventory.
				Map<Integer, Double> maxInputResourceRates = definition.getMaxInputRates();
				for (Entry<Integer, Double> input : maxInputResourceRates.entrySet()) {
					Integer resource = input.getKey();
					double maxRate = input.getValue();
					double resourceRate = maxRate * level;
					double resourceAmount = resourceRate * accumulatedTime;
					double stored = settlement.getAmountResourceStored(resource);
					
					// Get resource bottleneck
					double desiredResourceAmount = maxRate * time;
					double proportionAvailable = 1D;
					if (desiredResourceAmount > 0D)
						proportionAvailable = stored / desiredResourceAmount;
					if (bottleneck > proportionAvailable)
						bottleneck = proportionAvailable;
					
					// Retrieve the right amount
					if (stored > SMALL_AMOUNT) {
						if (resourceAmount > stored) {
							logger.warning(settlement, 30_000, "Case A. Just used up all '" + ResourceUtil.findAmountResourceName(resource)
								+ "' input to start '" + name + "'. Still missing " + Math.round(resourceAmount * 1000.0)/1000.0 + " kg. "
								+ Math.round(stored * 1000.0)/1000.0 + " kg in storage.");
							resourceAmount = stored;
							settlement.retrieveAmountResource(resource, resourceAmount);
							setProcessRunning(false);
							break;
							// Note: turn on a yellow flag and indicate which the input resource is missing
						}
						else
							settlement.retrieveAmountResource(resource, resourceAmount);
						
					}
					else {
						logger.warning(settlement, 30_000, "Case B. Not enough '" + ResourceUtil.findAmountResourceName(resource)
							+ "' input to start '" + name + "'. Still missing " + Math.round(resourceAmount * 1000.0)/1000.0 + " kg. "
							+ Math.round(stored * 1000.0)/1000.0 + " kg in storage.");
						setProcessRunning(false);
						break;
					}
				}

				// Set level
				if (level > bottleneck)
					level = bottleneck;
				
				// Output resources to inventory.
				Map<Integer, Double> maxOutputResourceRates = definition.getMaxOutputRates();
				for (Entry<Integer, Double> output : maxOutputResourceRates.entrySet()) {
					Integer resource = output.getKey();
					double maxRate = output.getValue();
					double resourceRate = maxRate * level;
					double resourceAmount = resourceRate * accumulatedTime;
					double remainingCap = settlement.getAmountResourceRemainingCapacity(resource);
					
					// Store the right amount
					if (remainingCap > SMALL_AMOUNT) {
						if (resourceAmount > remainingCap) {
							logger.warning(settlement, 30_000, "Case C. Just used up all remaining space for storing '" 
									+ ResourceUtil.findAmountResourceName(resource)
									+ "' output in '" + name + "'. Requiring " + Math.round((resourceAmount - remainingCap) * 1000.0)/1000.0 
									+ " kg of storage. Remaining cap: 0 kg.");
							resourceAmount = remainingCap;
							settlement.storeAmountResource(resource, resourceAmount);
							setProcessRunning(false);						
							break;
							// Note: turn on a yellow flag and indicate which the output resource is missing
						}
						else
							settlement.storeAmountResource(resource, resourceAmount);
						
					}
					else {
						logger.warning(settlement, 30_000, "Case D. Not enough space for storing '" 
								+ ResourceUtil.findAmountResourceName(resource)
								+ "' output to continue '" + name + "'. Requiring " + Math.round(resourceAmount * 1000.0)/1000.0 
								+ " kg of storage.. Remaining cap: " + Math.round(remainingCap * 1000.0)/1000.0 + " kg.");
						setProcessRunning(false);
						break;
					}
				}
				
				// Compute the remaining accumulatedTime
				accumulatedTime = accumulatedTime - PROCESS_CHECK_FREQUENCY;
				
			} else
				level = 0D;

			// Set the current production level.
			currentProductionLevel = level;
		}
	}

	/**
	 * Finds the bottleneck of input resources from inventory pool.
	 *
	 * @param time      (millisols)
	 * @param inventory the inventory pool the process uses.
	 * @return bottleneck (0.0D - 1.0D)
	 * @throws Exception if error getting input bottleneck.
	 */
	private double getInputBottleneck(double time, Settlement settlement) {

		// Check for illegal argument.
		if (time < 0D)
			throw new IllegalArgumentException("time must be > 0D");

		double bottleneck = 1D;
		Map<Integer, Double> maxInputResourceRates = definition.getMaxInputRates();
		for (Entry<Integer, Double> input : maxInputResourceRates.entrySet()) {
			Integer resource = input.getKey();
			double maxRate = input.getValue();
			double desiredResourceAmount = maxRate * time;
			double stored = settlement.getAmountResourceStored(resource);
			double proportionAvailable = 1D;
			if (desiredResourceAmount > 0D)
				proportionAvailable = stored / desiredResourceAmount;
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
	 * Checks if it has exceeded the time limit.
	 *
	 * @return
	 */
	public boolean isToggleAvailable() {
		int sol = marsClock.getMissionSol();
		int millisol = marsClock.getMillisolInt();
		if (sol == timeLimit[0]) {
            return millisol > timeLimit[1];
		}
		else {
            return millisol > timeLimit[1] + 1000;
		}
	}


	/**
	 * Gets the time permissions for the next toggle.
	 * 
	 * @return
	 */
	public int[] getTimeLimit() {
		return timeLimit;
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param clock
	 */
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}

	/**
	 * Resets the toggle time from teh current baseline time.
	 * 
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

	/**
	 * Times of the toggle operation. First item is the toggle work executed, 2nd is the target.
	 * 
	 * @return
	 */
	public double[] getToggleSwitchDuration() {
		return new double[] {toggleRunningWorkTime, definition.getToggleDuration()};
	}

}
