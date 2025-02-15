/*
 * Mars Simulation Project
 * ResourceProcess.java
 * @date 2024-06-09
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.io.Serializable;
import java.util.Set;

import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.ResourceProcessEngine;
import com.mars_sim.core.structure.building.ResourceProcessSpec;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The ResourceProcess class represents a process of converting one set of
 * resources to another. This represent the actual process instant attached to a Building.
 */
public class ResourceProcess implements ScheduledEventHandler, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResourceProcess.class.getName());

	private static final double RATE_FACTOR = 10;
	private static final double INPUT_BIAS = 0.9;
	private static final double MATERIAL_BIAS = 3;
	
	private static final double SMALL_AMOUNT = 0.000001;
	// How often should the process be checked? 
	private static final double PROCESS_CHECK_FREQUENCY = 5D; // 200 times per sol
	
	/** Flag for active change. */
	private boolean workerAssigned = false;
	/** is this process running ? */
	private boolean runningProcess;
	
	/** The level of effort for this resource process. */	
	private int level = 2;
	
	/** The time accumulated [in millisols]. */
	private double accumulatedTime;
	private double currentProductionLevel;
	private double toggleRunningWorkTime;

	/** The total score for this process. */
	private double score;
	/** The input score for this process. */	
	private double inputScore;
	/** The output score for this process. */	
	private double outputScore;

	private boolean canToggle = false;
	private MarsTime toggleDue = null; 

	private ResourceProcessEngine engine;
	private ResourceProcessSpec processSpec;
	private Settlement host;

	/**
	 * Constructor.
	 *
	 * @param engine The processing engine that this Process manages
	 */
	public ResourceProcess(ResourceProcessEngine engine, Settlement host) {
		this.processSpec = engine.getProcessSpec();
		runningProcess = processSpec.getDefaultOn();
		currentProductionLevel = 1D;
		this.canToggle = false;
		this.engine = engine;
		this.host = host;

		// Add some randomness, today is sol 1
		resetToggleWait(100 + RandomUtil.getRandomInt(processSpec.getProcessTime()));
	}

	
	@Override
	public int execute(MarsTime currentTime) {
		canToggle = true;
		return 0;
	}


	@Override
	public String getEventDescription() {
		return "Toggle " + (runningProcess ? "Off" : "On") + " for " + processSpec.getName();
	}

	/**
	 * Gets the process name.
	 *
	 * @return process name as string.
	 */
	public String getProcessName() {
		return processSpec.getName();
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

		int delay = processSpec.getProcessTime();
		if (!runningProcess) {
			// Not runnign so half the time before it can be restarted
			delay /= 2;
		}
		resetToggleWait(delay);
	}

	private void resetToggleWait(int delay) {
		var event = host.getFutureManager().addEvent(delay, this);
		toggleDue = event.getWhen();
	}

	/**
	 * Checks if the process has been flagged for change.
	 *
	 * @return true if the process has been flagged for change.
	 */
	public boolean isWorkerAssigned() {
		return workerAssigned;
	}

	/**
	 * Flags the process for change.
	 *
	 * @param value true if the flag is true.
	 */
	public void setWorkerAssigned(boolean value) {
		workerAssigned = value;
	}
	
	/**
	 * Adds work time to toggling the process on or off.
	 *
	 * @param time the amount (millisols) of time to add.
	 * @return true if done
	 */
	public boolean addToggleWorkTime(double time) {
		toggleRunningWorkTime += time;
		if (toggleRunningWorkTime >= processSpec.getWorkTime()) {
			toggleRunningWorkTime = 0D;
			canToggle = false;
			
			setProcessRunning(!runningProcess);
			
			return true;
		}
		
		return false;
	}

	public double getRemainingToggleWorkTime() {
		double time = processSpec.getWorkTime() - toggleRunningWorkTime;
		if (time > 0)
			return time;
		else
			return 0;
	}
		
	public double getOverallScore() {
		return score;
	}
	
	public void setOverallScore(double score) {
		this.score = score;
	}
	
	public double getInputScore() {
		return inputScore;
	}
	
	public void setInputScore(double inputScore) {
		this.inputScore = inputScore;
	}
	
	public double getOutputScore() {
		return outputScore;
	}
	
	public void setOutputScore(double outputScore) {
		this.outputScore = outputScore;
	}
	
	/**
	 * Gets the set of input resources.
	 *
	 * @return set of resources.
	 */
	public Set<Integer> getInputResources() {
		return processSpec.getInputResources();
	}

	public int getNumModules() {
		return engine.getModules();
	}
	
	/**
	 * Gets the base single input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getBaseSingleInputRate(Integer resource) {
		return processSpec.getBaseInputRate(resource);
	}

	/**
	 * Gets the base full input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getBaseFullInputRate(Integer resource) {
		return engine.getBaseFullInputRate(resource);

	}
	
	/**
	 * Checks if resource is an ambient input.
	 *
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(Integer resource) {
		return processSpec.isAmbientInputResource(resource);
	}

	/**
	 * Is this an in-situ resource ?
	 * 
	 * @param resource
	 * @return
	 */
	private boolean isInSitu(int resource) {
		return ResourceUtil.isInSitu(resource);
	}
	
	/**
	 * Is this a raw material resource ?
	 * 
	 * @param resource
	 * @return
	 */
	private boolean isRawMaterial(int resource) {
		return ResourceUtil.isRawMaterial(resource);
	}
	
	
	
	/**
	 * Gets the set of output resources.
	 *
	 * @return set of resources.
	 */
	public Set<Integer> getOutputResources() {
		return processSpec.getOutputResources();
	}

	/**
	 * Gets the base single output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getBaseSingleOutputRate(Integer resource) {
		return processSpec.getBaseOutputRate(resource);
	}

	/**
	 * Gets the base full output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getBaseFullOutputRate(Integer resource) {
		return engine.getBaseFullOutputRate(resource);
	}
	
	/**
	 * Checks if resource is a waste output.
	 *
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(Integer resource) {
		return processSpec.isWasteOutputResource(resource);
	}

	/**
	 * Processes resources for a given amount of time.
	 *
	 * @param pulse
	 * @param productionLevel proportion of max process rate (0.0D - 1.0D)
	 * @throws Exception if error processing resources.
	 */
	public void processResources(ClockPulse pulse, double productionLevel) {
		double time = pulse.getElapsed();

		if ((productionLevel < 0D) || (productionLevel > 1D) || (time < SMALL_AMOUNT))
			return;

		if (runningProcess) {
			double newProdLevel = productionLevel;

			accumulatedTime += time;

			double newCheckPeriod = PROCESS_CHECK_FREQUENCY * time;
			
			if (accumulatedTime >= newCheckPeriod) {
				// Compute the remaining accumulatedTime
				accumulatedTime -= newCheckPeriod;
	
				double bottleneck = 1D;

				// Input resources from inventory.
				for (Integer resource : processSpec.getInputResources()) {
					if (!processSpec.isAmbientInputResource(resource)) {
						double fullRate = getBaseFullInputRate(resource);
						double resourceRate = fullRate * newProdLevel;
						double required = resourceRate * accumulatedTime;
						double stored = host.getAmountResourceStored(resource);
						
						// Get resource bottleneck
						double desiredResourceAmount = fullRate * time;
						double proportionAvailable = 1D;
						if (desiredResourceAmount > 0D)
							proportionAvailable = stored / desiredResourceAmount;
						if (bottleneck > proportionAvailable)
							bottleneck = proportionAvailable;
						
						// Retrieve the right amount
						if (stored > SMALL_AMOUNT) {
							if (required > stored) {
								logger.fine(host, 30_000, "Case A. Used up all '" + ResourceUtil.findAmountResourceName(resource)
									+ "' input to start '" + processSpec.getName() + "'. Required: " + Math.round(required * 1000.0)/1000.0 + " kg. Remaining: "
									+ Math.round(stored * 1000.0)/1000.0 + " kg in storage.");
								required = stored;
								host.retrieveAmountResource(resource, required);
								setProcessRunning(false);
								break;
								// Note: turn on a yellow flag and indicate which the input resource is missing
							}
							else
								host.retrieveAmountResource(resource, required);
							
						}
						else {
							logger.fine(host, 30_000, "Case B. Not enough '" + ResourceUtil.findAmountResourceName(resource)
								+ "' input to start '" + processSpec.getName() + "'. Required: " + Math.round(required * 1000.0)/1000.0 + " kg. Remaining: "
								+ Math.round(stored * 1000.0)/1000.0 + " kg in storage.");
							setProcessRunning(false);
							break;
						}
					}
				}

				// Set level
				newProdLevel = Math.min(newProdLevel, bottleneck);
				
				// Output resources to inventory.
				for (Integer resource : processSpec.getOutputResources()) {
						double maxRate = getBaseFullOutputRate(resource);
						double resourceRate = maxRate * newProdLevel;
						double required = resourceRate * accumulatedTime;
						double remainingCap = host.getAmountResourceRemainingCapacity(resource);
						
						// Store the right amount
						if (remainingCap > SMALL_AMOUNT) {
							if (required > remainingCap) {
								logger.fine(host, 30_000, "Case C. Used up all remaining space for storing '" 
										+ ResourceUtil.findAmountResourceName(resource)
										+ "' output in '" + processSpec.getName() + "'. Required: " + Math.round((required - remainingCap) * 1000.0)/1000.0 
										+ " kg of storage. Remaining cap: 0 kg.");
								required = remainingCap;
								host.storeAmountResource(resource, required);
								setProcessRunning(false);						
								break;
								// Note: turn on a yellow flag and indicate which the output resource is missing
							}
							else
								host.storeAmountResource(resource, required);
							
						}
						else {
							logger.fine(host, 30_000, "Case D. Not enough space for storing '" 
									+ ResourceUtil.findAmountResourceName(resource)
									+ "' output to continue '" + processSpec.getName() + "'. Required: " + Math.round(required * 1000.0)/1000.0 
									+ " kg of storage. Remaining cap: " + Math.round(remainingCap * 1000.0)/1000.0 + " kg.");
							setProcessRunning(false);
							break;
						}
				}
			}

			// Set the current production level.
			currentProductionLevel = newProdLevel;
		}
	}


	/**
	 * Gets the string value for this object.
	 *
	 * @return string
	 */
	public String toString() {
		return getProcessName();
	}

	/**
	 * Gets the amount of power required to run the process.
	 *
	 * @return power (kW).
	 */
	public double getPowerRequired() {
		return processSpec.getPowerRequired();
	}

	/**
	 * Checks if the process has exceeded the time limit.
	 *
	 * @return
	 */
	public boolean canToggle() {
		return canToggle;
	}

	/**
	 * Gets the time permissions for the next toggle.
	 * 
	 * @return Maybe null if no toggle scheduled
	 */
	public MarsTime getToggleDue() {
		return toggleDue;
	}

	/**
	 * Times of the toggle operation. First item is the toggle work executed, 2nd is the target.
	 * 
	 * @return
	 */
	public double[] getToggleSwitchDuration() {
		return new double[] {toggleRunningWorkTime, processSpec.getWorkTime()};
	}

	/**
	 * Checks if a resource process has all input resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @return false if any input resources are empty.
	 */
	public boolean isInputsPresent(Settlement settlement) {
		double stored = 0;
		for (int resource: getInputResources()) {
			if (!isAmbientInputResource(resource)) {
				stored += settlement.getAmountResourceStored(resource);
			}
			else {
				// Note: the ambient resource is always available
				return true;
			}
		}
		if (stored < SMALL_AMOUNT) {
			return false;
		}
		
		return true;
	}


	/**
	 * Checks if a resource process has no output resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @return true if any output resources are emptywast
	 */
	public boolean isOutputsEmpty(Settlement settlement) {
		double stored = 0;
		for (int resource : getOutputResources()) {
			stored += settlement.getAmountResourceStored(resource);
		}
		if (stored < SMALL_AMOUNT) {
			return true;
		}
		return false;
	}

	
	/**
	 * Gets the total value of a resource process's input or output.
	 *
	 * @param settlement the settlement for the resource process.
	 * @param input      is the resource value for the input?
	 * @return the total value for the input or output.
	 */
	public double computeResourcesValue(Settlement settlement, boolean input) {
		double score = 0;

		Set<Integer> set = null;
		if (input)
			set = getInputResources();
		else
			set = getOutputResources();

		GoodsManager gm = settlement.getGoodsManager();
		for (int resource : set) {
			// Gets the vp for this resource
			// Add 1 to avoid being less than 1
			double vp = gm.getGoodValuePoint(resource);

			// Gets the supply of this resource
			// Note: use supply instead of stored amount.
			// Stored amount is slower and more time consuming
			double supply = gm.getSupplyValue(resource);

			if (input) {
				// For inputs: 
				// Note: mass rate is kg/sol
				double rate = getBaseSingleInputRate(resource) * RATE_FACTOR;

				// The original mass rate without being affected by vp and supply
//				baseMassRate += rate / supply;
				
				// Multiply by bias so as to favor/discourage the production of output resources

				// Calculate the modified mass rate
				double mrate = rate * vp * INPUT_BIAS;
				
				// Note: mass rate * VP -> demand
				
				// if this resource is ambient
				// that the settlement doesn't need to supply (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (isAmbientInputResource(resource)) {
					// e.g. For CO2, limit the score
					score += mrate;
				} else if (isInSitu(resource) || isRawMaterial(resource)) {
					// If in-situ, reduce the input score 
					score += mrate / MATERIAL_BIAS * Math.max(60, supply);
				} else {
					score += mrate * supply;
				}
			}

			else {
				// For outputs: 
				// Gets the remaining amount of this resource
				double remain = settlement.getAmountResourceRemainingCapacity(resource);

				if (remain == 0.0)
					return 0;

				double rate = getBaseSingleOutputRate(resource) * RATE_FACTOR;

				// For output value
				if (rate > remain) {
					// This limits the rate to match the remaining space 
					// that can accommodate this output resource
					rate = remain;
				}
				// The original mass rate without being affected by vp and supply
//				baseMassRate += rate / supply;
				// Calculate the modified mass rate
				double mrate = rate * vp * (.1 + level/1.5);
				
				// if this resource is ambient or a waste product
				// that the settlement won't keep (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (isWasteOutputResource(resource)) {
					score += mrate;
				} else if (isInSitu(resource) || isRawMaterial(resource)) {
					// If in-situ, increase the output score 
					score += mrate * supply * MATERIAL_BIAS;
				} else
					score += mrate * supply;
			}
		}

		return score;
	}
}
