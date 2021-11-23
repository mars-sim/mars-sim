/*
 * Mars Simulation Project
 * ToggleResourceProcess.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Management;
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

	/** Task phases. */
	private static final TaskPhase TOGGLING = new TaskPhase(Msg.getString("Task.phase.toggleProcess")); //$NON-NLS-1$
	private static final TaskPhase FINISHED = new TaskPhase(Msg.getString("Task.phase.toggleProcess.finished")); //$NON-NLS-1$

	private static final String OFF = "off";
	private static final String ON = "on";

	// Data members
	/** True if process is to be turned on, false if turned off. */
	private boolean toBeToggledOn;
	/** True if the finished phase of the process has been completed. */
	private boolean finished = false;

	/** The resource process to toggle. */
	private ResourceProcess process;
	/** The building the resource process is in. */
	private Building resourceProcessBuilding;
	/** The building the person can go to remotely control the resource process. */
	private Building destination;

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 */
	public ToggleResourceProcess(Person person) {
        super(NAME_ON, person, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D, 5D + RandomUtil.getRandomInt(5));

        if (person.isInSettlement()) {
			process = selectResourceProcess(person);
			if (process != null) {
				// Copy the current state of this process running
				toBeToggledOn = !process.isProcessRunning();

				if (!toBeToggledOn) {
					setName(NAME_OFF);
					setDescription(NAME_OFF);
				}
				else {
					setDescription(NAME_ON);
				}

            	Management m = resourceProcessBuilding.getManagement();
    			if (m != null) {
    				destination = resourceProcessBuilding;
    				walkToTaskSpecificActivitySpotInBuilding(destination, FunctionType.MANAGEMENT, false);
    			}

				else {
					boolean done = false;
					// Pick an administrative building for remote access to the resource building
					List<Building> admins = person.getSettlement().getBuildingManager()
							.getBuildings(FunctionType.ADMINISTRATION);

					if (!admins.isEmpty()) {

						List<Building> adminsNotFull = new ArrayList<>();

						for (Building b : admins) {
							if (b.getBuildingType().toLowerCase().equals(C2)) {
								destination = b;
								walkToTaskSpecificActivitySpotInBuilding(b, FunctionType.RESOURCE_PROCESSING, false);
								done = true;
								break;
							}
							else if (b.getAdministration() != null && !b.getAdministration().isFull()) {
								adminsNotFull.add(b);
							}
						}

						if (!done) {
							if (!adminsNotFull.isEmpty()) {
								int rand = RandomUtil.getRandomInt(admins.size()-1);
								destination = admins.get(rand);
								walkToTaskSpecificActivitySpotInBuilding(destination, FunctionType.RESOURCE_PROCESSING,
																		 false);
							}
							else {
								endTask();
								logger.log(person, Level.WARNING, 20_000, process.getProcessName()
											+ ": Adminstration space unavailable.");
							}
						}
					}
					else {
						endTask();
						logger.log(person, Level.WARNING, 20_000, process.getProcessName()
									+ ": Adminstration space unavailable.");
					}
				}


				addPhase(TOGGLING);
				addPhase(FINISHED);

				setPhase(TOGGLING);
	        }
	        else {
	        	endTask();
	        	logger.log(person, Level.WARNING, 20_000, "No ResourceProcess available.");
	        }
        }
        else {
        	endTask();
        	logger.log(person, Level.WARNING, 20_000, "Not in Settlement.");

        }
	}

	/**
	 * Gets the building at a person's settlement with the resource process that
	 * needs toggling.
	 *
	 * @param person the person.
	 * @return building with resource process to toggle, or null if none.
	 */
	public static Building getResourceProcessingBuilding(Person person) {
		Building result = null;

		Settlement settlement = person.getSettlement();
		if (settlement != null) {
			BuildingManager manager = settlement.getBuildingManager();
			double bestDiff = 0D;
			Iterator<Building> i = manager.getBuildings(FunctionType.RESOURCE_PROCESSING).iterator();
			while (i.hasNext()) {
				Building building = i.next();
				// In this building, select the best resource to compete
				ResourceProcess process = getResourceProcess(building);
				if (process != null && process.isToggleAvailable()) {
					double diff = getResourcesValueDiff(settlement, process);
					if (diff > bestDiff) {
						bestDiff = diff;
						result = building;
					}
				}
			}
		}

		return result;
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
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the resource process that needs toggling.
	 *
	 * @param person the person.
	 * @return the resource process to toggle or null if none.
	 */
	public ResourceProcess selectResourceProcess(Person person) {
		ResourceProcess result = null;

		Settlement settlement = person.getSettlement();
		if (settlement != null) {
			BuildingManager manager = settlement.getBuildingManager();
			double bestDiff = 0D;
			Iterator<Building> i = manager.getBuildings(FunctionType.RESOURCE_PROCESSING).iterator();
			while (i.hasNext()) {
				Building building = i.next();
				// In this building, select the best resource to compete
				ResourceProcess process = getResourceProcess(building);
				if (process != null && process.isToggleAvailable()) {
					double diff = getResourcesValueDiff(settlement, process);
					if (diff > bestDiff) {
						bestDiff = diff;
						result = process;
						resourceProcessBuilding = building;
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
		double diff = (outputValue - inputValue) / inputValue;

		// Subtract power required per millisol.
		double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
		double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();
		diff -= powerValue;

		if (process.isProcessRunning()) {
			diff *= -1D;
		}

		// Check if settlement is missing one or more of the output resources.
		if (isEmptyOutputResourceInProcess(settlement, process)) {
			diff *= 2D;
		}

		// Check if settlement is missing one or more of the input resources.
		if (isEmptyInputResourceInProcess(settlement, process)) {
			if (process.isProcessRunning()) {
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

		Iterator<Integer> i = null;
		if (input)
			i = process.getInputResources().iterator();
		else
			i = process.getOutputResources().iterator();

		while (i.hasNext()) {
			int resource = i.next();
			boolean useResource = !input || !process.isAmbientInputResource(resource);
            if (!input && process.isWasteOutputResource(resource)) {
				useResource = false;
			}
			if (useResource) {
				// Gets the demand for this resource
				double demand = settlement.getGoodsManager().getAmountDemandValue(resource);
				double rate = 0D;
//				double cap = settlement.getInventory().getAmountResourceCapacity(resource, false);
				double remain = settlement.getAmountResourceRemainingCapacity(resource);
//				double stored = cap - remain;

				if (input) {
					rate = process.getMaxInputResourceRate(resource);

					// For input value, the higher the stored,
					if (rate > remain) {
						rate = remain;
					}
					result += (rate / demand);

				} else {
					rate = process.getMaxOutputResourceRate(resource);

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
				if (stored == 0D) {
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
			if (stored == 0D) {
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
			endTask();
		}

		if (isDone()) {
			// if the work has been accomplished (it takes some finite amount of time to
			// finish the task
			endTask();
			return time;
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
		process.addToggleWorkTime(workTime);

		// Add experience points
		addExperience(time);

		// Check if the process has already been completed by another person.
		if (process.isProcessRunning() == toBeToggledOn) {
			setPhase(FINISHED);
		}

		// Check if an accident happens during the manual toggling.
		if (destination == resourceProcessBuilding) {
			checkForAccident(resourceProcessBuilding, time, 0.005D);
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

		if (!finished) {
			String toggle = OFF;
			if (toBeToggledOn) {
				toggle = ON;
				process.setProcessRunning(true);
			}
			else {
				process.setProcessRunning(false);
			}

			if (destination == resourceProcessBuilding) {
				logger.log(destination, person, Level.FINE, 0,
						   "Manually turned " + toggle + " " + process.getProcessName()
						   + " in " + resourceProcessBuilding.getNickName()
						   + ".");
			}
			else {
				logger.log(destination, person, Level.FINE, 0,
							"Turned " + toggle + " remotely " + process.getProcessName()
					       + " in " + resourceProcessBuilding.getNickName()
					       + ".");
			}
			// Only need to run the finished phase once and for all
			finished = true;
		}

		return 0D;
	}

	@Override
	protected double performMappedPhase(double time) {
//		time = super.performMappedPhase(time);
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
