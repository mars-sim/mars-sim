/*
 * Mars Simulation Project
 * ToggleResourceProcess.java
 * @date 2021-12-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The ToggleResourceProcess class is an EVA task for toggling a particular
 * automated resource process on or off.
 */
public class ToggleResourceProcess extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ToggleResourceProcess.class.getName());

	/** Task name */
	private static final String NAME_ON = Msg.getString("Task.description.toggleResourceProcess.on"); //$NON-NLS-1$
	private static final String NAME_OFF = Msg.getString("Task.description.toggleResourceProcess.off"); //$NON-NLS-1$

	private static final String C2 = "command and control";

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .25D;

	private static final double SMALL_AMOUNT = 0.000001;
	
	/** Task phases. */
	private static final TaskPhase TOGGLING = new TaskPhase(Msg.getString("Task.phase.toggleProcess")); //$NON-NLS-1$
	private static final TaskPhase FINISHED = new TaskPhase(Msg.getString("Task.phase.toggleProcess.finished")); //$NON-NLS-1$

	private static final String OFF = "off";
	private static final String ON = "on";

	// Data members
	/** True if process is to be turned on, false if turned off. */
	private boolean toBeToggledOn;
	/** True if the finished phase of the process has been completed. */
	private boolean isFinished = false;

	/** The resource process to be toggled. */
	private ResourceProcess process;
	/** The building the resource process is in. */
	private Building resourceProcessBuilding;

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 */
	public ToggleResourceProcess(Person person) {
        super(NAME_ON, person, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D, 10D);

        SimpleEntry<Building, ResourceProcess> entry = ToggleResourceProcess.getResourceProcessingBuilding(person);
		resourceProcessBuilding = entry.getKey();
		process = entry.getValue();
		
		if (resourceProcessBuilding != null || process != null) {
	        if (person.isInSettlement()) {
	        	setupResourceProcess();
	        }
	        else {
	        	end("Not in Settlement.");
	        }
		}
		else {
			end("No resource processes found.");
		}
	}

	/**
	 * Sets up the resource process
	 */
	private void setupResourceProcess() {
		// Copy the current state of this process running
		boolean state = !process.isProcessRunning();

		if (!state) {
			setName(NAME_OFF);
			setDescription(NAME_OFF);
		}
		else {
			setDescription(NAME_ON);
		}

		ResourceProcessing rp = resourceProcessBuilding.getResourceProcessing();
		if (rp != null) {
			walkToResourceBldg(resourceProcessBuilding);
		}

		else {
            // Looks for management function for toggling resource process.
			checkManagement();
		}

		addPhase(TOGGLING);
		addPhase(FINISHED);

		setPhase(TOGGLING);
	}
	
	/**
	 * Check if any management function is available
	 */
	private void checkManagement() {

		boolean done = false;
		// Pick an administrative building for remote access to the resource building
		List<Building> mgtBuildings = person.getSettlement().getBuildingManager()
				.getBuildings(FunctionType.MANAGEMENT);

		if (!mgtBuildings.isEmpty()) {

			List<Building> notFull = new ArrayList<>();

			for (Building b : mgtBuildings) {
				if (b.hasFunction(FunctionType.ADMINISTRATION)) {
					walkToMgtBldg(b);
					done = true;
					break;
				}
				else if (b.getManagement() != null && !b.getManagement().isFull()) {
					notFull.add(b);
				}
			}

			if (!done) {
				if (!notFull.isEmpty()) {
					int rand = RandomUtil.getRandomInt(mgtBuildings.size()-1);
					walkToMgtBldg(mgtBuildings.get(rand));
				}
				else {
					end(process.getProcessName() + ": Management space unavailable.");
				}
			}
		}
		else {
			end("Management space unavailable.");
		}
	}

	/**
	 * Ends the task
	 *
	 * @param s
	 */
	private void end(String s) {
		logger.log(person, Level.WARNING, 20_000, s);
		endTask();
	}

	/**
     * Walks to the building with resource processing function
     *
     * @param b the building
     */
	private void walkToResourceBldg(Building b) {
		walkToTaskSpecificActivitySpotInBuilding(b,
				FunctionType.RESOURCE_PROCESSING,
				false);
	}

	/**
     * Walks to the building with management function
     *
     * @param b the building
     */
	private void walkToMgtBldg(Building b) {
		walkToTaskSpecificActivitySpotInBuilding(b,
				FunctionType.MANAGEMENT,
				false);
	}


	/**
	 * Gets the building at a person's settlement with the resource process that
	 * needs toggling.
	 *
	 * @param person the person.
	 * @return SimpleEntry containing the building with the resource process to toggle, or null if none.
	 */
	public static SimpleEntry<Building, ResourceProcess> getResourceProcessingBuilding(Person person) {
		Building building0 = null;
		ResourceProcess process0 = null;
		
		Settlement settlement = person.getSettlement();
		if (settlement != null) {
			double bestDiff = 0D;
			Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING).iterator();
			while (i.hasNext()) {
				Building building = i.next();
				// In this building, select the best resource to compete
				ResourceProcess process = getResourceProcess(building);
				if (process != null && process.isToggleAvailable() && !process.isFlagged()) {
					double diff = getResourcesValueDiff(settlement, process);
					if (diff > bestDiff) {
						bestDiff = diff;
						building0 = building;
						process0 = process;
					}
				}
			}
		}

		return new SimpleEntry<>(building0, process0);
	}

	/**
	 * Gets the resource process to toggle at a building.
	 *
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 */
	public static ResourceProcess getResourceProcess(Building building) {
		ResourceProcess result = null;

		Settlement settlement = building.getSettlement();
		if (building.hasFunction(FunctionType.RESOURCE_PROCESSING)) {
			double bestDiff = 0D;
			ResourceProcessing processing = building.getResourceProcessing();
			Iterator<ResourceProcess> i = processing.getProcesses().iterator();
			while (i.hasNext()) {
				ResourceProcess process = i.next();
				if (process.isToggleAvailable()) {
					double diff = getResourcesValueDiff(settlement, process);
					if (diff > bestDiff) {
						bestDiff = diff;
						result = process;
//						logger.info(building, 20_000, process.getProcessName() + " diff: " + Math.round(diff * 1000.0)/1000.0);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the resources value diff between inputs and outputs for a resource
	 * process.
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource value diff (value points)
	 */
	public static double getResourcesValueDiff(Settlement settlement, ResourceProcess process) {
		double inputValue = getResourcesValue(settlement, process, true);
		double outputValue = getResourcesValue(settlement, process, false);
		double diff = 0.01;

		if (inputValue > 0 && inputValue > SMALL_AMOUNT) {
			diff = (outputValue - inputValue) / inputValue;
		}

		// Subtract power required per millisol.
		double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
		double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();
		diff -= powerValue;

		if (process.isProcessRunning()) {
			// most likely don't need to change the status of this process 
			diff *= -1D;
		}

		// Check if settlement is missing one or more of the output resources.
		if (isEmptyOutputResourceInProcess(settlement, process)) {
			// will need to execute the task of toggling on this process to produce more output resources
			diff *= 2D;
		}

		// NOTE: Need to detect if the output resource is dwindling 
		
		// Check if settlement is missing one or more of the input resources.
		if (isEmptyInputResourceInProcess(settlement, process)) {
			if (process.isProcessRunning()) {
				// will need to execute the task of toggling off this process 
				diff *= 1D;
			} else {
				diff = 0D;
			}
		}
		
		return diff;
	}

	/**
	 * Gets the total value of a resource process's input or output.
	 *
	 * @param settlement the settlement for the resource process.
	 * @param process    the resource process.
	 * @param input      is the resource value for the input?
	 * @return the total value for the input or output.
	 */
	private static double getResourcesValue(Settlement settlement, ResourceProcess process, boolean input) {

		double result = 0D;

		Set<Integer> set = null;
		if (input)
			set = process.getInputResources();
		else
			set = process.getOutputResources();

		for (int resource: set) {
			boolean useResource = !input || !process.isAmbientInputResource(resource);
            if (!input && process.isWasteOutputResource(resource)) {
				useResource = false;
			}
			if (useResource) {
				// Gets the demand for this resource
				double demand = settlement.getGoodsManager().getAmountDemandValue(resource);
				double remain = settlement.getAmountResourceRemainingCapacity(resource);

				if (input) {
					double rate = process.getMaxInputRate(resource);

					// For input value, the higher the stored,
					if (rate > remain) {
						rate = remain;
					}
					result += (rate / demand);

				} else {
					double rate = process.getMaxOutputRate(resource);

					// For output value, the
					if (rate > remain) {
						rate = remain;
					}
					result += (rate * demand);
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a resource process has no input resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @param process    the resource process.
	 * @return true if any input resources are empty.
	 */
	private static boolean isEmptyInputResourceInProcess(Settlement settlement, ResourceProcess process) {
		boolean result = false;

		Iterator<Integer> i = process.getInputResources().iterator();
		while (i.hasNext()) {
			int resource = i.next();
			if (!process.isAmbientInputResource(resource)) {
				double stored = settlement.getAmountResourceStored(resource);
				if (stored < SMALL_AMOUNT) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a resource process has no output resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @param process    the resource process.
	 * @return true if any output resources are empty.
	 */
	private static boolean isEmptyOutputResourceInProcess(Settlement settlement, ResourceProcess process) {
		boolean result = false;

		Iterator<Integer> i = process.getOutputResources().iterator();
		while (i.hasNext()) {
			int resource = i.next();
			double stored = settlement.getAmountResourceStored(resource);
			if (stored < SMALL_AMOUNT) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Performs the toggle process phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double togglingPhase(double time) {
	
		double perf = person.getPerformanceRating();
		// If person is incapacitated, enter airlock.
		if (perf == 0D) {
			// reset it to 10% so that he can walk inside
			person.getPhysicalCondition().setPerformanceFactor(.1);
			end(": poor performance in " + process.getProcessName());
		}

		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = getEffectiveSkillLevel();
		if (mechanicSkill == 0) {
			workTime /= 2;
		}
		if (mechanicSkill > 1) {
			workTime += workTime * (.2D * mechanicSkill);
		}

		// Add work to the toggle process.
		if (process.addToggleWorkTime(workTime)) {
			toBeToggledOn = !process.isProcessRunning();
			setPhase(FINISHED);
		}

		// Add experience points
		addExperience(time);

		// Check if an accident happens during the manual toggling.
		if (resourceProcessBuilding != null) {
			checkForAccident(resourceProcessBuilding, time, 0.005D);
		}
		
		if (isDone()) {
			// if the work has been accomplished (it takes some finite amount of time to
			// finish the task
			endTask();
			return time;
		}

		return 0;
	}

	/**
	 * Performs the finished phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	protected double finishedPhase(double time) {
				
		if (!isFinished) {
			String toggle = OFF;
			if (toBeToggledOn) {
				toggle = ON;
				process.setProcessRunning(true);
			}
			else {
				process.setProcessRunning(false);
			}

			// Reset the change flag to false
			process.setFlag(false);
			
			if (resourceProcessBuilding != null) {
				logger.log(person, Level.INFO, 1_000,
						   "Manually turned " + toggle + " " + process.getProcessName()
						   + " in " + resourceProcessBuilding.getName()
						   + ".");
			}
			else {
				logger.log(person, Level.INFO, 1_000,
							"Turned " + toggle + " remotely " + process.getProcessName()
					       + " in " + person.getBuildingLocation().getName()
					       + ".");
			}
			
			// Only need to run the finished phase once and for all
			isFinished = true;
		}
		
		return 0D;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (TOGGLING.equals(getPhase())) {
			return togglingPhase(time);
		} else if (FINISHED.equals(getPhase())) {
			return finishedPhase(time);
		} else {
			return time;
		}
	}
}
