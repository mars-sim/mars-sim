/*
 * Mars Simulation Project
 * ResourceProcess.java
 * @date 2024-06-09
 * @author Scott Davis
 */
package com.mars_sim.core.resourceprocess;

import java.util.Set;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The ResourceProcess class represents a process of converting one set of
 * resources to another. This represent the actual process instant attached to a Building.
 */
public class ResourceProcess implements ScheduledEventHandler {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResourceProcess.class.getName());

	private static final double SMALL_AMOUNT = 0.000001;
	// How often should the process be checked? 
	private static final double PROCESS_CHECK_FREQUENCY = 5D; // 200 times per sol
	
	/**
	 * Represents the internal state of the process.
	 */
	public enum ProcessState {
			RUNNING, IDLE, INPUTS_UNAVAILABLE
	}

	private boolean workerAssigned = false;
	private boolean isRunning;
	
	private int levelOfEffort = 3;
	
	/** The time accumulated [in millisols]. */
	private double accumulatedTime;
	private double currentProductionLevel;
	private double toggleRunningWorkTime;
	private double dutyTime;
	private double cumulativeMillisols;
	
	private ResourceProcessAssessment assessment;

	private boolean canToggle = false;
	private MarsTime toggleDue = null; 

	private ResourceProcessEngine engine;
	private ResourceProcessSpec processSpec;
	private Building building;

	public static final ResourceProcessAssessment DEFAULT_ASSESSMENT = new ResourceProcessAssessment(0, 0, 0, false);
	
	/**
	 * Constructor.
	 *
	 * @param engine The processing engine that this Process manages
	 */
	public ResourceProcess(ResourceProcessEngine engine, Building building) {
		this.processSpec = engine.getProcessSpec();
		isRunning = processSpec.getDefaultOn();
		currentProductionLevel = 1D;
		this.canToggle = false;
		this.engine = engine;
		this.building = building;
		this.assessment = DEFAULT_ASSESSMENT;

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
		return "Toggle " + (isRunning ? "Off" : "On") + " - " + processSpec.getName();
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
		return isRunning;
	}

	/**
	 * Checks if the process has required inputs.
	 * This is not a live instantaneous check, but a check of the last time the process was run.
	 *
	 * @return true if process has inputs
	 */
	public ProcessState getState() {
		if (isRunning) {
			return ProcessState.RUNNING;
		}
		else if (assessment.inputsAvailable()) {
			return ProcessState.IDLE;
		}
		else {
			return ProcessState.INPUTS_UNAVAILABLE;
		}
	}

	/**
	 * Sets if the process is running or not.
	 *
	 * @param newRunning true if process is running.
	 */
	public void setProcessRunning(boolean newRunning) {
		// Record completion
		if (isRunning && !newRunning) {
			// Record the completion
			building.getAssociatedSettlement().recordProcess(processSpec.getName(), "Resource", building);
		}

		this.isRunning = newRunning;

		int delay = processSpec.getProcessTime();
		if (!isRunning) {
			// Not running so half the time before it can be restarted
			delay /= 2;
		}
		resetToggleWait(delay);
	}

	private void resetToggleWait(int delay) {
		var event = building.getAssociatedSettlement().getFutureManager().addEvent(delay, this);
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
			
			setProcessRunning(!isRunning);
			
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
		return assessment.overallScore();
	}
	
	public double getInputScore() {
		return assessment.inputScore();
	}
	
	public double getOutputScore() {
		return assessment.outputScore();
	}
	
	/**
	 * Gets the percentage of duty time.
	 * 
	 * @return
	 */
	public double getPercentDuty() {
		return dutyTime / cumulativeMillisols * 100; 
	}
	
	/**
	 * Sets the level of effort.
	 * 
	 * @param level
	 */
	public void setLevel(int level) {
		levelOfEffort = level;
	}
	
	
	public void setAssessment(ResourceProcessAssessment assessment) {
		this.assessment = assessment;
	}

	/**
	 * Gets the specification of this process.
	 * 
	 * @return
	 */
	public ResourceProcessSpec getSpec() {
		return processSpec;
	}

	/**
	 * Gets the minimum processing time for this process.
	 * 
	 * @return Value in mSol
	 */
	public int getProcessTime() {
		return processSpec.getProcessTime();
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
	 * @param cumulativeMillisols
	 * @throws Exception if error processing resources.
	 */
	public void processResources(ClockPulse pulse, double productionLevel, double cumulativeMillisols) {
		double time = pulse.getElapsed();

		this.cumulativeMillisols = cumulativeMillisols;
		
		if ((productionLevel < 0D) || (productionLevel > 1D) || (time < SMALL_AMOUNT))
			return;

		if (isRunning) {
			
			var host = building.getAssociatedSettlement();
			
			double newProdLevel = productionLevel;
			// Set the current production level.
			currentProductionLevel = newProdLevel * levelOfEffort / 5;
			
			accumulatedTime += time;

			double newCheckPeriod = PROCESS_CHECK_FREQUENCY * time;
			
			if (accumulatedTime >= newCheckPeriod) {
				// Compute the remaining accumulatedTime
				accumulatedTime -= newCheckPeriod;	
				// Increment the duty time here
				dutyTime += time;

				// Input resources from inventory.
				for (Integer resource : processSpec.getInputResources()) {
					if (!processSpec.isAmbientInputResource(resource)) {
						double fullRate = getBaseFullInputRate(resource);
						double resourceRate = fullRate * currentProductionLevel;
						double required = resourceRate * accumulatedTime;
						if (required == 0D)
							continue;

						double stored = host.getSpecificAmountResourceStored(resource);

						// Retrieve the right amount
						if (stored > SMALL_AMOUNT) {
							if (required > stored) {
								required = stored;
								// Alter the amount required to whatever required amount
								// and retrieve that amount
								host.retrieveAmountResource(resource, required);							
								// Halt the process now 
								resourceProblem(resource, false, required, stored);

								break;
							}
							else
								host.retrieveAmountResource(resource, required);
						}
						else {
							// Halt the process now 
							resourceProblem(resource, false, required, stored);

							break;
						}
					}
				}
				
				// Output resources to inventory.
				for (Integer resource : processSpec.getOutputResources()) {
					double maxRate = getBaseFullOutputRate(resource);
					double resourceRate = maxRate * currentProductionLevel;
					double required = resourceRate * accumulatedTime;
					double remainingCap = host.getRemainingCombinedCapacity(resource);
					
					// Store the right amount
					if (remainingCap > SMALL_AMOUNT) {
						if (required > remainingCap) {
							required = remainingCap;
							// Alter the amount required to whatever required amount
							// and store that amount
							host.storeAmountResource(resource, required);
							// Halt the process now 
							resourceProblem(resource, true, required, remainingCap);
							
							break;
						}
						else {
							host.storeAmountResource(resource, required);						
						}
						
					}
					else {
						// Halt the process now 
						resourceProblem(resource, true, required, remainingCap);
						
						break;
					}
				}
			}
		}
	}

	/**
	 * Prints the resource problem and stops the process.
	 * 
	 * @param resource
	 * @param capacity
	 * @param required
	 * @param available
	 */
	private void resourceProblem(int resource, boolean capacity, double required, double available) {
		logger.info(building, 10_000,
					(capacity ? "No capacity '" : "Not enough '")
					+ ResourceUtil.findAmountResourceName(resource)
					+ "' for '" + processSpec.getName() + "'. Required: "
					+ Math.round(required * 1000.0)/1000.0 + " kg. Available: "
					+ Math.round(available * 1000.0)/1000.0 + " kg.");
		setProcessRunning(false);
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
	 * Gets the amount of energy required to run the process.
	 *
	 * @return energy (kWh).
	 */
	public double getkWhRequired() {
		// No need of checking if (isProcessRunning()) since 
		// ResourceProcessor::getCombinedPowerLoad will check 
		// if a process is running
		return processSpec.getkWhRequired() * getNumModules();
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
}
