/*
 * Mars Simulation Project
 * ResourceProcess.java
 * @date 2022-07-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Set;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.ResourceProcessEngine;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

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
	
	/** Flag for active change. */
	private boolean flag = false;
	/** is this process running ? */
	private boolean runningProcess;
	
	/** The level of effort for this resource process. */	
	private int level = 2;
	
	/** The time accumulated [in millisols]. */
	private double accumulatedTime;

	private double currentProductionLevel;

	private double toggleRunningWorkTime;

	private String name;

	private int[] timeLimit = new int[] {1, 0};

	private ResourceProcessEngine engine;

	private static MasterClock clock;

	/**
	 * Constructor.
	 *
	 * @param engine The processing engine that this Process manages
	 */
	public ResourceProcess(ResourceProcessEngine engine) {
		this.name = engine.getName();
		runningProcess = engine.getDefaultOn();
		currentProductionLevel = 1D;
		this.engine = engine;

		// Add some randomness, today is sol 1
		resetToggleTime(1, 100 + RandomUtil.getRandomInt(engine.getProcessTime()));
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
	 * Adds work time to toggling the process on or off.
	 *
	 * @param time the amount (millisols) of time to add.
	 * @return true if done
	 */
	public boolean addToggleWorkTime(double time) {
		toggleRunningWorkTime += time;
		if (toggleRunningWorkTime >= engine.getWorkTime()) {
			toggleRunningWorkTime = 0D;
			
			runningProcess = !runningProcess;

			// Reset for next toggle
			MarsTime now = clock.getMarsTime();
			resetToggleTime(now.getMissionSol(), now.getMillisolInt());
			
			return true;
		}
		
		return false;
	}

	public double getRemainingToggleWorkTime() {
		double time = engine.getWorkTime() - toggleRunningWorkTime;
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
		return engine.getInputResources();
	}

	public int getNumModules() {
		return engine.getModules();
	}
	
	/**
	 * Gets the max input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputRate(Integer resource) {
		return engine.getMaxInputRate(resource);
	}

	/**
	 * Checks if resource is an ambient input.
	 *
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(Integer resource) {
		return engine.isAmbientInputResource(resource);
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
		return engine.getOutputResources();
	}

	/**
	 * Gets the max output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxOutputRate(Integer resource) {
		return engine.getMaxOutputRate(resource);
	}

	/**
	 * Checks if resource is a waste output.
	 *
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(Integer resource) {
		return engine.isWasteOutputResource(resource);
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

			double newCheckPeriod = PROCESS_CHECK_FREQUENCY * time;
			
			if (accumulatedTime >= newCheckPeriod) {
				// Compute the remaining accumulatedTime
				accumulatedTime -= newCheckPeriod;
//				logger.info(settlement, 30_000, name + "  pulse width: " + Math.round(time * 10000.0)/10000.0 
//						+ "  accumulatedTime: " + Math.round(accumulatedTime * 100.0)/100.0 
//						+ "  processInterval: " + processInterval);
	
				double bottleneck = 1D;

				// Input resources from inventory.
				for(Integer resource : engine.getInputResources()) {
					double maxRate = engine.getMaxInputRate(resource);
					double resourceRate = maxRate * level;
					double required = resourceRate * accumulatedTime;
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
						if (required > stored) {
							logger.fine(settlement, 30_000, "Case A. Used up all '" + ResourceUtil.findAmountResourceName(resource)
								+ "' input to start '" + name + "'. Required: " + Math.round(required * 1000.0)/1000.0 + " kg. Remaining: "
								+ Math.round(stored * 1000.0)/1000.0 + " kg in storage.");
							required = stored;
							settlement.retrieveAmountResource(resource, required);
							setProcessRunning(false);
							break;
							// Note: turn on a yellow flag and indicate which the input resource is missing
						}
						else
							settlement.retrieveAmountResource(resource, required);
						
					}
					else {
						logger.fine(settlement, 30_000, "Case B. Not enough '" + ResourceUtil.findAmountResourceName(resource)
							+ "' input to start '" + name + "'. Required: " + Math.round(required * 1000.0)/1000.0 + " kg. Remaining: "
							+ Math.round(stored * 1000.0)/1000.0 + " kg in storage.");
						setProcessRunning(false);
						break;
					}
				}

				// Set level
				if (level > bottleneck)
					level = bottleneck;
				
				// Output resources to inventory.
				for(Integer resource : engine.getOutputResources()) {
					double maxRate = engine.getMaxOutputRate(resource);
					double resourceRate = maxRate * level;
					double required = resourceRate * accumulatedTime;
					double remainingCap = settlement.getAmountResourceRemainingCapacity(resource);
					
					// Store the right amount
					if (remainingCap > SMALL_AMOUNT) {
						if (required > remainingCap) {
							logger.fine(settlement, 30_000, "Case C. Used up all remaining space for storing '" 
									+ ResourceUtil.findAmountResourceName(resource)
									+ "' output in '" + name + "'. Required: " + Math.round((required - remainingCap) * 1000.0)/1000.0 
									+ " kg of storage. Remaining cap: 0 kg.");
							required = remainingCap;
							settlement.storeAmountResource(resource, required);
							setProcessRunning(false);						
							break;
							// Note: turn on a yellow flag and indicate which the output resource is missing
						}
						else
							settlement.storeAmountResource(resource, required);
						
					}
					else {
						logger.fine(settlement, 30_000, "Case D. Not enough space for storing '" 
								+ ResourceUtil.findAmountResourceName(resource)
								+ "' output to continue '" + name + "'. Required: " + Math.round(required * 1000.0)/1000.0 
								+ " kg of storage. Remaining cap: " + Math.round(remainingCap * 1000.0)/1000.0 + " kg.");
						setProcessRunning(false);
						break;
					}
				}
			}

			// Set the current production level.
			currentProductionLevel = level;
		}
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
		return engine.getPowerRequired();
	}

	/**
	 * Checks if it has exceeded the time limit.
	 *
	 * @return
	 */
	public boolean isToggleAvailable() {
		MarsTime now = clock.getMarsTime();
		int sol = now.getMissionSol();
		int millisol = now.getMillisolInt();
		if (sol == timeLimit[0]) {
            return millisol > timeLimit[1];
		}
		else {
			// Toggleing has rolled over the day
            return sol > timeLimit[0];
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
	 * @param masterClock
	 */
	public static void initializeInstances(MasterClock masterClock) {
		clock = masterClock;
	}

	/**
	 * Resets the toggle time from teh current baseline time.
	 * 
	 * @param sol Baseline mission sol
	 * @param millisols
	 */
	private void resetToggleTime(int sol, int millisols) {
		// Compute the time limit
		millisols += engine.getProcessTime();
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
		return new double[] {toggleRunningWorkTime, engine.getWorkTime()};
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	/**
	 * Checks if a resource process has all input resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @return false if any input resources are empty.
	 */
	public boolean isInputsPresent(Settlement settlement) {

		for(int resource: getInputResources()) {
			if (!isAmbientInputResource(resource)) {
				double stored = settlement.getAmountResourceStored(resource);
				if (stored < SMALL_AMOUNT) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Checks if a resource process has no output resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @return true if any output resources are empty.
	 */
	public boolean isEmptyOutputs(Settlement settlement) {

		for(int resource : getOutputResources()) {
			double stored = settlement.getAmountResourceStored(resource);
			if (stored < SMALL_AMOUNT) {
				return true;
			}
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
	public double getResourcesValue(Settlement settlement, boolean input) {
		double score = 0D;
		double benchmarkValue = 0;

		Set<Integer> set = null;
		if (input)
			set = getInputResources();
		else
			set = getOutputResources();

		GoodsManager gm = settlement.getGoodsManager();
		for (int resource : set) {
			// Gets the vp for this resource
			double vp = gm.getGoodValuePoint(resource);

			// Gets the supply of this resource
			// Note: use supply instead of stored amount.
			// Stored amount is slower and more time consuming
			double supply = gm.getSupplyValue(resource);

			if (input) {

				double rate = getMaxInputRate(resource) / getNumModules() * 1000;
				// Limit the vp so as to favor the production of output resources
				double modVp = Math.max(.01, Math.min(20, vp / 10));
				// The original value without being affected by vp and supply
				benchmarkValue += rate;

				// if this resource is ambient
				// that the settlement doesn't need to supply (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (isAmbientInputResource(resource)) {
					score += rate / Math.max(10, supply) / 10;
				} else if (isInSitu(resource)) {
					score += rate / supply / supply / 10;
				} else
					score += ((rate * modVp) / supply);
			}

			else {
				// Gets the remaining amount of this resource
				double remain = settlement.getAmountResourceRemainingCapacity(resource);

				if (remain == 0.0)
					return 0;

				double rate = getMaxOutputRate(resource) / getNumModules() * 1000;

				// For output value
				if (rate > remain) {
					rate = remain;
				}

				benchmarkValue += rate;

				// if this resource is ambient or a waste product
				// that the settlement won't keep (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (isWasteOutputResource(resource)) {
					score += rate * (1 + level) * 2;
				} else
					score += ((rate * vp) / supply) * (1 + level) * 2;
			}
		}

		return (score - benchmarkValue);
	}
}
