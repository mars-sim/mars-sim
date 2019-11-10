/**
 * Mars Simulation Project
 * ToggleResourceProcess.java
 * @version 3.1.0 2017-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Administration;
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
	private static Logger logger = Logger.getLogger(ToggleResourceProcess.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** The minimum period of time in millisols the process must stay on or off. */
	public static final int DURATION = 200;

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

	/** True if toggling process is EVA operation. */
//	private boolean needEVA;
	
	/** The resource process to toggle. */
	private ResourceProcess process;
	/** The building the resource process is in. */
	private Building resourceProcessBuilding;
	/** The building the person can go to remotely control the resource process. */
	private Building destination;

	private static MarsClock marsClock;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public ToggleResourceProcess(Person person) {
        super(NAME_ON, person, true, false, STRESS_MODIFIER, true, 5D + RandomUtil.getRandomInt(5));
		//super(NAME_ON, person, false, 0D);

        if (person.isInSettlement()) {
//			resourceProcessBuilding = getResourceProcessingBuilding(person);
			process = selectResourceProcess(person);
			
			if (process != null) {
//			if (resourceProcessBuilding != null) {
//				process = getResourceProcess(resourceProcessBuilding);
							
				marsClock = Simulation.instance().getMasterClock().getMarsClock();
				
				// Compute the time limit
				int sol = marsClock.getMissionSol();
				int millisols = marsClock.getMillisolInt() + DURATION;
				if (millisols >= 1000) {
					millisols = millisols - 1000;
					sol = sol + 1;
				}				
				// Tag this particular process for toggling
				process.setTimeLimit(sol, millisols);
				// Copy the current state of this process running 
				toBeToggledOn = !process.isProcessRunning();

				if (!toBeToggledOn) {
					setName(NAME_OFF);
					setDescription(NAME_OFF);
				}
				else {
					setDescription(NAME_ON);
				}
				
				// Assume the toggling is NOW handled remotely in the command control center
//	            needEVA = false;
				
				Administration admin = resourceProcessBuilding.getAdministration();
				if (admin != null && !admin.isFull()) {
					destination = resourceProcessBuilding;
					walkToActivitySpotInBuilding(destination, false);
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
								walkToActivitySpotInBuilding(b, false);
								done = true;
								break;
							}
							else if (b.getAdministration() != null && !b.getAdministration().isFull()) {
								adminsNotFull.add(b);
							}
						}
						// TODO: find the closest one
						
						if (!done && !adminsNotFull.isEmpty()) {
							int rand = RandomUtil.getRandomInt(admins.size()-1);
							destination = admins.get(rand);
							walkToActivitySpotInBuilding(destination, false);
						}

						else
							endTask();
					}
					
					else
						endTask();
				}

				
				// NOTE : no longer require EVA to physically go out there and manually touching
				// a toggle switch

//	            needEVA = !building.hasFunction(FunctionType.LIFE_SUPPORT);
	//
//	            // If habitable building, add person to building.
//	            if (!needEVA) {
//	                // Walk to building.
//	                walkToActivitySpotInBuilding(building, false);
//	            }
//	            else {
//	                // Determine location for toggling resource source.
//	                Point2D toggleLoc = determineToggleLocation();
//	                setOutsideSiteLocation(toggleLoc.getX(), toggleLoc.getY());
//	            }

//			}

				addPhase(TOGGLING);
				addPhase(FINISHED);
				
	//			if (!needEVA) {
					setPhase(TOGGLING);
	//			}
	        }
	        else
	        	endTask();
        }
        else
        	endTask();
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.RESOURCE_PROCESSING;
	}

//	/**
//	 * Determine location to toggle resource source.
//	 * 
//	 * @return location.
//	 */
//	private Point2D determineToggleLocation() {
//
//		Point2D.Double newLocation = new Point2D.Double(0D, 0D);
//
//		boolean goodLocation = false;
//		for (int x = 0; (x < 50) && !goodLocation; x++) {
//			Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(resourceProcessBuilding, 1D);
//			newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
//					resourceProcessBuilding);
//			goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
//					person.getCoordinates());
//		}
//
//		return newLocation;
//	}

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
				if (process != null && process.hasPassedTimeLimit()) {
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
				if (process.hasPassedTimeLimit()) {
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
				if (process != null && process.hasPassedTimeLimit()) {
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
		double diff = outputValue - inputValue;

		// Subtract power required per millisol.
//		double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
		double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
		double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();
		diff -= powerValue;

		if (process.isProcessRunning()) {
			diff *= -1D;
		}

		// Check if settlement doesn't have one or more of the input resources.
		if (isEmptyInputResourceInProcess(settlement, process)) {
			if (process.isProcessRunning()) {
				diff = 1D;
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
			boolean useResource = true;
			if (input && process.isAmbientInputResource(resource)) {
				useResource = false;
			}
			if (!input && process.isWasteOutputResource(resource)) {
				useResource = false;
			}
			if (useResource) {
				double value = settlement.getGoodsManager().getGoodsDemandValue(resource);
				double rate = 0D;
				if (input) {
					rate = process.getMaxInputResourceRate(resource);
				} else {
					rate = process.getMaxOutputResourceRate(resource);
					double storageCapacity = settlement.getInventory().getAmountResourceRemainingCapacity(resource,
							true, false);
					if (rate > storageCapacity) {
						rate = storageCapacity;
					}
				}
				result += (value * rate);
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
				double stored = settlement.getInventory().getAmountResourceStored(resource, false);
				if (stored == 0D) {
					result = true;
				}
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

//		if (resourceProcessBuilding != null) {
//			process = getResourceProcess(resourceProcessBuilding);
		double perf = person.getPerformanceRating();
		// If person is incapacitated, enter airlock.
		if (perf == 0D) {
			// reset it to 10% so that he can walk inside
			person.getPhysicalCondition().setPerformanceFactor(.1);
//			if (needEVA) {
//				setPhase(WALK_BACK_INSIDE);
//			} else {
				endTask();
//			}
		}

		if (isDone()) {
			// if the work has been accomplished (it takes some finite amount of time to
			// finish the task
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
//			System.out.println("go to the finished phase");
			setPhase(FINISHED);
//			if (needEVA) {
//				setPhase(WALK_BACK_INSIDE);
//			} else {
//				endTask();
//			}
		}

		// Check if an accident happens during the manual toggling.
		if (destination == resourceProcessBuilding) {
			checkForAccident(time);
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
			Settlement s = person.getSettlement(); 	
			
			String toggle = OFF;
			if (toBeToggledOn) {
				toggle = ON;
				process.setProcessRunning(true);
			}
			else {
				process.setProcessRunning(false);
			}
	
			if (destination == resourceProcessBuilding) {
				LogConsolidated.log(logger, Level.INFO, 0, sourceName,
						"[" + s.getName() + "] " + person.getName() + " at " + destination.getNickName() 
						+ " manually turned the " + process.getProcessName()  + " " + toggle + ".", null);
			}
			else {
				LogConsolidated.log(logger, Level.INFO, 0, sourceName,
						"[" + s.getName() + "] " + person.getName() + " at " + destination.getNickName() 
					+ " gained remote access to the " + process.getProcessName() 
					+ " of " + resourceProcessBuilding.getNickName() + " and turned it " + toggle + ".", null);
			}
			// Only need to run the finished phase once and for all
			finished = true;
		}
		
		return 0D;
	}
	
	

	@Override
	protected void addExperience(double time) {

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;

//		if (needEVA) {
//			// Add experience to "EVA Operations" skill.
//			// (1 base experience point per 100 millisols of time spent)
//			double evaExperience = time / 100D;
//			evaExperience += evaExperience * experienceAptitudeModifier;
//			evaExperience *= getTeachingExperienceModifier();
//			person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
//		}

		// If phase is toggle process, add experience to mechanics skill.
		if (TOGGLING.equals(getPhase())) {
			// 1 base experience point per 100 millisols of time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 100D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience, time);
		}
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> result = new ArrayList<SkillType>(2);
		result.add(SkillType.MECHANICS);
//		if (needEVA) {
//			result.add(SkillType.EVA_OPERATIONS);
//		}
		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
//		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
//		if (needEVA) {
//			return (int) Math.round((double) (EVAOperationsSkill + mechanicsSkill) / 2D);
//		} else {
			return (mechanicsSkill);
//		}
	}

//	@Override
//	protected TaskPhase getOutsideSitePhase() {
//		return TOGGLE_PROCESS;
//	}

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
	
	/**
	 * Check for accident with entity during toggle resource phase.
	 * 
	 * @param time the amount of time (in millisols)
	 */
	protected void checkForAccident(double time) {

		// Use EVAOperation checkForAccident() method.
//		if (needEVA) {
//			super.checkForAccident(time);
//		}

		double chance = .005D;

		// Mechanic skill modification.
		int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the building's wear condition.
		chance *= resourceProcessBuilding.getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			if (person != null) {
//	            logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " + person.getName() + " has an accident while toggling a resource process.");
				resourceProcessBuilding.getMalfunctionManager().createASeriesOfMalfunctions(person);
			} else if (robot != null) {
//				logger.info("[" + robot.getLocationTag().getShortLocationName() +  "] " + robot.getName() + " has an accident while toggling a resource process.");
				resourceProcessBuilding.getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}

		}
	}

	@Override
	public void destroy() {
		super.destroy();

		process = null;
		resourceProcessBuilding = null;
	}
}