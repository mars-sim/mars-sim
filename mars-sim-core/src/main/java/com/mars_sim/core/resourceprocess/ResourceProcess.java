/*
 * Mars Simulation Project
 * ResourceProcess.java
 * @date 2026-09-27
 * @author Scott Davis
 */
package com.mars_sim.core.resourceprocess;

import java.util.Set;

import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resourceprocess.task.ToggleResourceProcessMeta;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.MathUtils;
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
	
	/**
	 * Represents the internal state of the process.
	 */
	public enum ProcessState {
			RUNNING, IDLE, INPUTS_UNAVAILABLE, LOCK_ON
	}

	private boolean canToggle = false;
	private boolean workerAssigned = false;
	private boolean isRunning;
	private boolean isLockOn;
	
	private int modules;

	private int settlementMaxModules;
	
	private double percentEffort = 100.0;
	private double currentProductionLevel;
	private double toggleRunningWorkTime;
	private double dutyTime;
	private double cumulativeMillisols;
	
	private ResourceProcessAssessment assessment;
	private MarsTime toggleDue = null; 
	private ResourceProcessEngine engine;
	private ResourceProcessSpec processSpec;
//	private Building building;
	private Settlement settlement;
	private ResourceHolder host;
	
	public static final ResourceProcessAssessment DEFAULT_ASSESSMENT = new ResourceProcessAssessment(0, 0, 0, false);
	
//	/**
//	 * Constructor 1.
//	 *
//	 * @param engine The processing engine that this process manages
//	 * @param building
//	 */
//	public ResourceProcess(ResourceProcessEngine engine, Building building) {
//		this.processSpec = engine.getProcessSpec();
//		isRunning = processSpec.getDefaultOn();
//		currentProductionLevel = 1D;
//		this.canToggle = false;
//		this.engine = engine;
//		this.building = building;
//		this.assessment = DEFAULT_ASSESSMENT;
//		
//		this.modules = (int)MathUtils.between(engine.getMaxModules() / 3.0, 1, engine.getMaxModules());
//
//		this.host = building.getAssociatedSettlement().getEquipmentInventory();
//		
//		// Add some randomness at the start of the sim
//		int delay = RandomUtil.getRandomInt(0, 50);
//		resetToggleWait(delay);
//	}

	/**
	 * Constructor 2.
	 *
	 * @param engine The processing engine that this process manages
	 * @param settlement
	 */
	public ResourceProcess(ResourceProcessEngine engine, Settlement settlement) {
		this.processSpec = engine.getProcessSpec();
		isRunning = processSpec.getDefaultOn();
		currentProductionLevel = 1D;
		this.canToggle = false;
		this.engine = engine;
		this.settlement = settlement;
		this.assessment = DEFAULT_ASSESSMENT;
		
		int numM = 1;
		
		int maxMod = engine.getMaxModules();
		if (maxMod == 4)
			numM = 2;
		else if (maxMod > 4)
			numM = RandomUtil.getRandomInt((int)MathUtils.between(maxMod/4, 2, maxMod/2), (int)MathUtils.between(maxMod/2, 2, maxMod));
		
		this.modules += numM;
		
		addMaxModules(maxMod);
		
		this.host = settlement.getEquipmentInventory();
		
		// Add some randomness at the start of the sim
		int delay = RandomUtil.getRandomInt(0, 50);
		resetToggleWait(delay);
	}

	/**
	 * Adds a resource process.
	 * 
	 * @param max
	 */
	public void addMaxModules(int max) {
		settlementMaxModules += max;
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
			// Set the current production level.
			currentProductionLevel = productionLevel * (percentEffort / 100);
			// Increment the duty time here
			dutyTime += time;

			processInputResources(time);

			processOutputResources(time);
		}
	}

	/**
	 * Processes the input resources.
	 * 
	 * @param time in millisols
	 */
	private void processInputResources(double time) {
		// Input resources from inventory.
		for (Integer resource : processSpec.getInputResources()) {
			
			if (!processSpec.isAmbientInputResource(resource)) {
				// getCurrentInputRate factors in the number of modules
				double currentRate = getCurrentInputRate(resource) * time;
				double resourceRate = currentRate * currentProductionLevel;
				double required = resourceRate;
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
	}
	
	/**
	 * Processes the output resources.
	 * 
	 * @param time in millisols
	 */
	private void processOutputResources(double time) {
		// Output resources to inventory.
		for (Integer resource : processSpec.getOutputResources()) {
			
			if (!isWasteOutputResource(resource)) {	
				// getCurrentOutputRate factors in the number of modules
				double currentRate = getCurrentOutputRate(resource) * time;
				double resourceRate = currentRate * currentProductionLevel;
				double required = resourceRate;
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
	/**
	 * Prints the resource problem and stops the process.
	 * 
	 * @param resource
	 * @param capacity
	 * @param required
	 * @param available
	 */
	private void resourceProblem(int resource, boolean capacity, double required, double available) {
		// Do NOT delete. For debugging : 
//		logger.info(building, 10_000,
//					(capacity ? "No capacity '" : "Not enough '")
//					+ ResourceUtil.findAmountResourceName(resource)
//					+ "' for '" + processSpec.getName() + "'. Required: "
//					+ Math.round(required * 1000.0)/1000.0 + " kg. Available: "
//					+ Math.round(available * 1000.0)/1000.0 + " kg.");
		setProcessState(ProcessState.INPUTS_UNAVAILABLE);
	}

	/**
	 * Gets the amount of power required to run the process.
	 *
	 * @return energy (kW).
	 */
	public double getkWRequired() {
		// Note : No need of checking if (isProcessRunning()) since 
		// ResourceProcessor::getCombinedPowerLoad will check 
		// if a process is running
		return processSpec.getkWRequired() * getNumModules() * (percentEffort / 100);
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
			
			if (isRunning) {
				// Turn the running state into idle state
				setProcessState(ProcessState.IDLE);
			}
			else {
				// Turn the idle state into running state
				setProcessState(ProcessState.RUNNING);
			}
			
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
	 * Sets the percentage of effort.
	 * 
	 * @param Percent
	 */
	public void setPercentEffort(double Percent) {
		percentEffort = Percent;
	}
	
	/**
	 * Gets the percentage of effort.
	 * 
	 * @return
	 */
	public double getPercentEffort() {
		return percentEffort;
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


	/**
	 * Gets the max number of modules.
	 * 
	 * @return
	 */
	public final int getMaxModules() {
		return settlementMaxModules;
	}
	
	/**
	 * Gets the number of modules.
	 * 
	 * @return
	 */
	public int getNumModules() {
		return modules;
	}
	
    /**
     * Sets the number of modules for this resource process engine.
     */
    public void setModules(int value) {
    	modules = value;
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
	 * Gets the current input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getCurrentInputRate(Integer resource) {
		return getNumModules() * processSpec.getBaseInputRate(resource);
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
	 * Gets the current output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getCurrentOutputRate(Integer resource) {
		return getNumModules() * processSpec.getBaseOutputRate(resource);
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
	 * Checks if the process is locked-on.
	 *
	 * @return true if process is locked-on
	 */
	public boolean isProcessLockOn() {
		return isLockOn;
	}
	
	/**
	 * Checks if the process has required inputs.
	 * This is not a live instantaneous check, but a check of the last time the process was run.
	 *
	 * @return true if process has inputs
	 */
	public ProcessState getState() {
		if (isLockOn) {
			return ProcessState.LOCK_ON;
		}
		else if (isRunning) {
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
	 * Sets the process state.
	 *
	 * @param selected the process state
	 */
	public void setProcessState(ProcessState selected) {
		boolean newRunning = false;
		
		if (selected == ProcessState.RUNNING) {
			newRunning = true;
    	}
    	else if (selected == ProcessState.LOCK_ON) {
    		newRunning = true;
    		isLockOn = true;
    	}
    	else if (selected == ProcessState.IDLE) {
    		newRunning = false;
    		isLockOn = false;
    	}
    	else if (selected == ProcessState.INPUTS_UNAVAILABLE) {
    		newRunning = false;
    		isLockOn = false;
    		
//    		if (modules > 0) {
//    			increaseInputResourceDemand();
//				logger.info(building, getProcessName() + "'s # of modules : " + modules + " -> " + --modules);
//			}
    	}
			
		// If it used to be running and now it has stopped
		if (isRunning && !newRunning) {
			// Record the completion
			settlement.recordProcess(processSpec.getName(), "Resource", settlement.getName());
		}

		int delay = 0;
		
		if (newRunning) {
//			delay = processSpec.getProcessTime();
//			resetToggleWait(delay);
			
			// Q: when is it appropriate to call reduceOutputResourceDemand() to tone down the output resource demand ?
			
			adjustNumModules();
		}
		
		else {		
			delay = 20;
			resetToggleWait(delay);
		}
		
		this.isRunning = newRunning;
	}

	
	/**
	 * Adjusts the number of modules.
	 */
	public void adjustNumModules() {
		int overallScore = (int)getOverallScore();
		int outputScore = (int)getOutputScore();
		
		if (overallScore == 0 || outputScore == 0) {
//			logger.info(settlement, this + " - overallScore: " + overallScore + ". outputScore: " + outputScore);
			ToggleResourceProcessMeta.generateAssessment(settlement, modules, this);
		}
		
		if (getProcessName().equalsIgnoreCase("Melt Ice")) {
			System.out.println(this + ". overallScore: " + overallScore + ". outputScore: " + outputScore);
		}
		
		if (modules == 0) {
			logger.info(settlement, getProcessName() + "'s # of modules : " + modules + " -> " + ++modules);
		}
		else if ((getOverallScore() <= ToggleResourceProcessMeta.MAX_SCORE / 4 
				|| getOutputScore() <= ToggleResourceProcessMeta.MAX_SCORE / 8) 
					&& modules > 1) {
//			logger.info(settlement, " - " + this + ". rand: " + rand);
			int rand = RandomUtil.getRandomInt((int)ToggleResourceProcessMeta.MAX_SCORE / 4);
			if (rand > overallScore / 2 || rand > outputScore) {
				logger.info(settlement, getProcessName() + "'s # of modules : " + modules + " -> " + --modules);
			}
		}
		else { // if (modules < engine.getMaxModules()) {
			
			if (modules < engine.getMaxModules()
					&& (overallScore > ToggleResourceProcessMeta.MAX_SCORE 
					 || (outputScore > ToggleResourceProcessMeta.MAX_SCORE / 2))) {
				int rand = RandomUtil.getRandomInt(ToggleResourceProcessMeta.MAX_SCORE);
//				logger.info(settlement, " - " + this + ". rand: " + rand);
				if (getProcessName().equalsIgnoreCase("Melt Ice")) {
					System.out.println(this + ". rand: " + rand);
				}
				if (rand < overallScore / 2 || rand < outputScore) {
					if (getProcessName().equalsIgnoreCase("Melt Ice")) {
						System.out.println(this + ". IN !");
					}
					logger.info(settlement, getProcessName() + "'s # of modules : " + modules + " -> " + ++modules);
				}
			}
			else if (modules > 1) {
				int rand = RandomUtil.getRandomInt(ToggleResourceProcessMeta.MAX_SCORE);
//				logger.info(settlement, " - " + this + ". rand: " + rand);
				if (rand > overallScore / 2 || rand > outputScore) {
					logger.info(settlement, getProcessName() + "'s # of modules : " + modules + " -> " + --modules);
				}
			}
		}
	}
	
	/**
	 * Resets the toggle wait.
	 * 
	 * @param delay
	 */
	private void resetToggleWait(int delay) {
		var event = settlement.getFutureManager().addEvent(delay, this);
		toggleDue = event.getWhen();
	}

	/**
	 * Reduces the demand score of the output resource.
	 */
	private void reduceOutputResourceDemand() {
		Set<Integer> resources = getOutputResources();
		for (int r: resources) {
			settlement.getGoodsManager().reduceDemandScore(r, 1);
		}
	}
	
	/**
	 * Increases the demand score of the input resource.
	 */
	private void increaseInputResourceDemand() {
		Set<Integer> resources = getInputResources();
		for (int r: resources) {
			settlement.getGoodsManager().increaseDemandScore(r, 1);
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
	
	@Override
	public int execute(MarsTime currentTime) {
		canToggle = true;
		return 0;
	}

	@Override
	public String getEventDescription() {
		return "Toggle " + (isRunning ? "Off" : "On") + " - " + processSpec.getName();
	}

}
