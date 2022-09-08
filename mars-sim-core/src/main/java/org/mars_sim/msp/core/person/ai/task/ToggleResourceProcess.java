/*
 * Mars Simulation Project
 * ToggleResourceProcess.java
 * @date 2022-09-05
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
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
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
	private static final String TOGGLE_ON = Msg.getString("Task.description.toggleResourceProcess.on"); //$NON-NLS-1$
	private static final String TOGGLE_OFF = Msg.getString("Task.description.toggleResourceProcess.off"); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double FACTOR = 1_000;
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
	 * @param worker the worker performing the task.
	 */
	public ToggleResourceProcess(Worker worker) {
        super(TOGGLE_ON, worker, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D, 10D);

        SimpleEntry<Building, SimpleEntry<ResourceProcess, Double>> entry = worker.getSettlement().retrieveFirstResourceProcess();
        if (entry == null) {
        	entry = ToggleResourceProcess.getResourceProcessingBuilding(worker);
        }
        	
		resourceProcessBuilding = entry.getKey();
		process = entry.getValue().getKey();
		
		if (resourceProcessBuilding != null || process != null) {

	        if (worker.isInSettlement()) {
	        	setupResourceProcess();
	        }
	        else {
	        	clearTask("Not in Settlement.");
	        }
		}
		else {
			clearTask("No resource processes found.");
		}
	}

	/**
	 * Sets up the resource process.
	 */
	private void setupResourceProcess() {
		// Copy the current state of this process
		boolean state = process.isProcessRunning();

		if (!state) {
			setName(TOGGLE_OFF);
			setDescription(TOGGLE_OFF);
			logger.info(resourceProcessBuilding, worker + ": Toggling off '" + process + "'.");
		}
		else {
			setDescription(TOGGLE_ON);
			logger.info(resourceProcessBuilding, worker + ": Toggling on '" + process + "'.");
		}

		if (resourceProcessBuilding.hasFunction(FunctionType.LIFE_SUPPORT))
			walkToResourceBldg(resourceProcessBuilding);
		else
            // Looks for management function for toggling resource process.
			checkManagement();

		addPhase(TOGGLING);
		addPhase(FINISHED);

		setPhase(TOGGLING);
	}
	
	/**
	 * Checks if any management function is available.
	 */
	private void checkManagement() {

		boolean done = false;
		// Pick an administrative building for remote access to the resource building
		List<Building> mgtBuildings = worker.getSettlement().getBuildingManager()
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
					clearTask(process.getProcessName() + ": Management space unavailable.");
				}
			}
		}
		else {
			clearTask("Management space unavailable.");
		}
	}

	/**
	 * Ends the task.
	 *
	 * @param s
	 */
	private void clearTask(String s) {
		logger.log(worker, Level.INFO, 20_000, s);
		endTask();
	}

	/**
     * Walks to the building with resource processing function.
     *
     * @param b the building
     */
	private void walkToResourceBldg(Building b) {
		walkToTaskSpecificActivitySpotInBuilding(b,
				FunctionType.RESOURCE_PROCESSING,
				false);
	}

	/**
     * Walks to the building with management function.
     *
     * @param b the building
     */
	private void walkToMgtBldg(Building b) {
		walkToTaskSpecificActivitySpotInBuilding(b,
				FunctionType.MANAGEMENT,
				false);
	}


	/**
	 * Gets the building at a worker's settlement with the resource process that
	 * needs toggling.
	 *
	 * @param worker the worker.
	 * @return SimpleEntry containing the building with the resource process to toggle, or null if none.
	 */
	public static SimpleEntry<Building, SimpleEntry<ResourceProcess, Double>> getResourceProcessingBuilding(Worker worker) {
		Building selectedbuilding = null;
		ResourceProcess mostPosProcess = null;
		ResourceProcess mostNegProcess = null;
		double highest = 0;
		double lowest = 0;
		SimpleEntry<ResourceProcess, Double> entry = null;
	
		Settlement settlement = worker.getSettlement();
		if (settlement != null) {
			Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING).iterator();
			while (i.hasNext()) {
				Building building = i.next();
				// In this building, select the best resource to compete
				entry = selectMostPosNegResourceProcess(building);	
//				logger.info(worker, "" + building + "  entry: " + entry);
				// Check if this resource process is in the cache list.
				// If true, go to the next one
				if (settlement.getResourceProcessList().contains(entry)) {
					logger.info(building, "Already contained " + entry);
					continue;
				}
				
				ResourceProcess process = entry.getKey();
				double score = entry.getValue();
						
				if (process == null) {
//					logger.info(building, "process is null.");
					continue;
				}
			
				if (process != null && process.isToggleAvailable() && !process.isFlagged()) {
					if (score >= highest) {
						highest = score;
						mostPosProcess = process;
						selectedbuilding = building;
					}
					else if (score <= lowest) {
						lowest = score;
						mostNegProcess = process;
						selectedbuilding = building;
					}
				}
			}
		}
			
		SimpleEntry<Building, SimpleEntry<ResourceProcess, Double>> buildingProcess = null;
		
		if (mostPosProcess != null && highest >= Math.abs(lowest)) {
			buildingProcess = new SimpleEntry<>(selectedbuilding, new SimpleEntry<>(mostPosProcess, highest));
		}
		else if (mostNegProcess != null){
			buildingProcess = new SimpleEntry<>(selectedbuilding, new SimpleEntry<>(mostNegProcess, lowest));
		}
		
		return buildingProcess;
	}

	/**
	 * Select a resource process (from a building) based on its resource score.
	 *
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 */
	public static SimpleEntry<ResourceProcess, Double> selectMostPosNegResourceProcess(Building building) {
		ResourceProcess mostPosProcess = null;
		ResourceProcess mostNegProcess = null;
		double highest = 0;
		double lowest = 0;
		
		GoodsManager goodsManager = building.getSettlement().getGoodsManager();
		double regStored = building.getSettlement().getAmountResourceStored(ResourceUtil.regolithID);
		double iceStored = building.getSettlement().getAmountResourceStored(ResourceUtil.iceID);

		double hydrogenVP = goodsManager.getGoodValuePoint(ResourceUtil.hydrogenID);
		double methaneVP = goodsManager.getGoodValuePoint(ResourceUtil.methaneID);
		double waterVP = goodsManager.getGoodValuePoint(ResourceUtil.waterID);
		double oxygenVP = goodsManager.getGoodValuePoint(ResourceUtil.oxygenID);

		Iterator<ResourceProcess> i = building.getResourceProcessing().getProcesses().iterator();
		while (i.hasNext()) {
			ResourceProcess process = i.next();
			if (process.isToggleAvailable() && !process.isFlagged()) {
				double score = computeResourceScore(building.getSettlement(), process);
//				logger.info(building, process + "  score: " + score);
				
				// Check if settlement is missing one or more of the output resources.
				// Will multiply by 10 internally within computeResourcesValue() in ToggleResourceProcess
				if (isEmptyOutputResourceInProcess(building.getSettlement(), process)) {
					// will push for toggling on this process to produce more output resources
					if (process.isProcessRunning()) {
						// no need to change it
						continue;
					} else {
						// will need to push for toggling on this process since output resource is zero
						score *= FACTOR;
					}
				}
				
				// NOTE: Need to detect if the output resource is dwindling 

				// Check if settlement is missing one or more of the input resources.
				if (isEmptyInputResourceInProcess(building.getSettlement(), process)) {
					if (process.isProcessRunning()) {
						// will need to push for toggling off this process since input resource is insufficient 
						score *= FACTOR;
					} else {
						// no need to turn it on
						continue;
					}
				}
				
//				logger.info(worker, "1. " + process + "  result: " + result);
				
				if (score > 0 && process.isProcessRunning()) {
					// let it continue running. No need to turn it off.
					continue;
				}
				
				else if (score < 0 && process.isProcessRunning()) {
					// need to shut it down 
					score *= FACTOR;
				}

				else if (score > 0 && !process.isProcessRunning()) {
					// need to turn it on
					score *= FACTOR;
				}
				
				else if (score < 0 && !process.isProcessRunning()) {
					// let it continue not running. No need to turn it on.
					continue;
				}	
				
				String name = process.getProcessName().toLowerCase();
				
				boolean sab = name.equalsIgnoreCase(ResourceProcessing.SABATIER);
				boolean reg = name.contains(ResourceProcessing.REGOLITH);
				boolean ice = name.equalsIgnoreCase(ResourceProcessing.ICE);
				boolean ppa = name.equalsIgnoreCase(ResourceProcessing.PPA);
				boolean cfr = name.equalsIgnoreCase(ResourceProcessing.CFR);
				boolean ogs = name.equalsIgnoreCase(ResourceProcessing.OGS);
				
				if (reg) {
					score *= goodsManager.getAmountDemandValue(ResourceUtil.regolithID) * (1 + regStored * FACTOR);
				}

				else if (ice) {
					score *= goodsManager.getAmountDemandValue(ResourceUtil.iceID) * (1 + iceStored * FACTOR);
				}
				
				else if (ppa) {
					score *= hydrogenVP / methaneVP ;
				}
				
				else if (cfr) {
					score *= waterVP / hydrogenVP ;
				}
				
				else if (sab) {
					score *= waterVP * methaneVP / hydrogenVP;
				}

				else if (ogs) {
					score *= hydrogenVP * oxygenVP / waterVP;
				}
				
				// Randomize it to give other processes a chance
				if (score > 0.0)
					score = RandomUtil.getRandomDouble(score * .2, score * 5);
				else if (score < 0.0)
					score = - RandomUtil.getRandomDouble(-score * .2, -score * 5);
				
				if (score >= highest) {
					highest = score;
					mostPosProcess = process;
				}
				else if (score <= lowest) {
					lowest = score;
					mostNegProcess = process;
				}
			}
		}
		
		if (highest >= Math.abs(lowest))
			return new SimpleEntry<>(mostPosProcess, highest);
		else
			return new SimpleEntry<>(mostNegProcess, lowest);
	}

	/**
	 * Gets the resource process for toggling.
	 *
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 */
	public static ResourceProcess getResourceProcess(Building building) {
		ResourceProcess result = null;
		double bestDiff = 0D;
		Iterator<ResourceProcess> i = building.getResourceProcessing().getProcesses().iterator();
		while (i.hasNext()) {
			ResourceProcess process = i.next();
			if (process.isToggleAvailable()) {
				double score = computeResourceScore(building.getSettlement(), process);
				if (score > bestDiff) {
					bestDiff = score;
					result = process;
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets the composite resource score based on the ratio of 
	 * VPs of outputs to VPs of inputs for a resource process.
	 *
	 * @param settlement the settlement the resource process is at.
	 * @param process    the resource process.
	 * @return the resource score (0 = no need to change); positive number -> demand to turn on ; negative number -> demand to turn off 
	 */
	public static double computeResourceScore(Settlement settlement, ResourceProcess process) {
		double inputValue = computeResourcesValue(settlement, process, true);
		double outputValue = computeResourcesValue(settlement, process, false);
		
//		logger.info(settlement, process 
////				+ "   inputValue: " + (int)inputValue
////				+ "   outputValue: " + (int)outputValue
//				+ "   score: " + Math.round((outputValue - inputValue)*100.0)/100.0
//				);
		
		return outputValue - inputValue;
	}

	/**
	 * Gets the total value of a resource process's input or output.
	 *
	 * @param settlement the settlement for the resource process.
	 * @param process    the resource process.
	 * @param input      is the resource value for the input?
	 * @return the total value for the input or output.
	 */
	private static double computeResourcesValue(Settlement settlement, ResourceProcess process, boolean input) {
		double score = 0D;
		double benchmarkValue = 0;
		
		Set<Integer> set = null;
		if (input)
			set = process.getInputResources();
		else
			set = process.getOutputResources();

		for (int resource: set) {
			// Gets the vp for this resource
			double vp = settlement.getGoodsManager().getGoodValuePoint(resource);	

			// Gets the supply of this resource
			// Note: use supply instead of stored amount.
			// Stored amount is slower and more time consuming
			double supply = settlement.getGoodsManager().getSupplyValue(resource);
			
			if (input) {
				
				double rate = process.getMaxInputRate(resource) / process.getNumModules() * 1000;
				// Limit the vp so as to favor the production of output resources
				double modVp = Math.max(.01, Math.min(20, vp/10));
				// The original value without being affected by vp and supply
				benchmarkValue += rate;
				
				// if this resource is ambient 
				// that the settlement doesn't need to supply (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (process.isAmbientInputResource(resource)) {
					score += rate / supply;
				}
				else if (process.isInSitu(resource)) {
					score += rate / supply / supply;
				}
				else
					score += ((rate * modVp) / supply);
			}

			else {
				// Gets the remaining amount of this resource
				double remain = settlement.getAmountResourceRemainingCapacity(resource);
				
				if (remain == 0.0)
					return 0;
				
				double rate = process.getMaxOutputRate(resource) / process.getNumModules() * 1000;
				
				// For output value
				if (rate > remain) {
					rate = remain;
				}
				
				benchmarkValue += rate;
				
				// if this resource is ambient or a waste product 
				// that the settlement won't keep (e.g. carbon dioxide),
				// then it won't need to check how much it has in stock
				// and it will not be affected by its vp and supply
				if (process.isWasteOutputResource(resource)) {
					score += rate / supply;
				}
				else
					score += ((rate * vp) / supply);
			}
		}

//		String type = "input -"; 
//		
//		if (!input)
//			type = "output -";
//		
//		logger.info(settlement, process 
//				+ "   " + type
//				+ "   benchmarkValue: " + Math.round(benchmarkValue * 100.0)/100.0
//				+ "   value: " + Math.round(score * 100.0)/100.0
//				+ "   score: " + Math.round((score - benchmarkValue) * 100.0)/100.0
//				);
		
		return (score - benchmarkValue);
	}

	/**
	 * Checks if a resource process has no input resources.
	 *
	 * @param settlement the settlement the resource is at.
	 * @param process    the resource process.
	 * @return true if any input resources are empty.
	 */
	public static boolean isEmptyInputResourceInProcess(Settlement settlement, ResourceProcess process) {
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
	public static boolean isEmptyOutputResourceInProcess(Settlement settlement, ResourceProcess process) {
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

	/**
	 * Performs the toggle process phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double togglingPhase(double time) {
		double workTime = time;
		
		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = getEffectiveSkillLevel();
		if (mechanicSkill == 0) {
			workTime /= 2;
		}
		if (mechanicSkill > 1) {
			workTime += workTime * (.2D * mechanicSkill);
		}
		
		if (worker.getUnitType() == UnitType.PERSON) {
			double perf = worker.getPerformanceRating();
			// If worker is incapacitated, enter airlock.
			if (perf == 0D) {
				// reset it to 10% so that he can walk inside
				person.getPhysicalCondition().setPerformanceFactor(.1);
				clearTask(": poor performance in " + process.getProcessName());
			}
		
		}
		else {
			workTime /= 2;
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
			checkForAccident(resourceProcessBuilding, time, 0.005);
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
				
				if (resourceProcessBuilding.hasFunction(FunctionType.LIFE_SUPPORT))
					logger.log(worker, Level.INFO, 1_000,
						   "Manually toggled " + toggle + " '" + process.getProcessName()
						   + "' in " + resourceProcessBuilding.getName()
						   + ".");
				else
					logger.log(worker, Level.INFO, 1_000,
							"Remotely toggled " + toggle + " '" + process.getProcessName()
					       + "' in " + worker.getBuildingLocation().getName()
					       + ".");
			}
			
			// Only need to run the finished phase once and for all
			isFinished = true;
			
			endTask();
		}
		
		return 0D;
	}


}
