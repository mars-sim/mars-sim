/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 3.1.0 2017-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The TendGreenhouse class is a task for tending the greenhouse in a
 * settlement. This is an effort driven task.
 */
public class TendGreenhouse extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TendGreenhouse.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendGreenhouse"); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase SAMPLING = new TaskPhase(Msg.getString("Task.phase.sampling")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase TRANSFERRING_SEEDLING = new TaskPhase(Msg.getString("Task.phase.transferring")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase GROWING_TISSUE = new TaskPhase(Msg.getString("Task.phase.growingTissue")); //$NON-NLS-1$	

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	/** The total time spent in inspecting the greenhouse. */
	private double timeInspecting;
	/** The total time spent in inspecting the greenhouse. */
	private double timeCleaning;
	/** The total time spent in inspecting the greenhouse. */
	private double timeSampling;
	
	
	// Data members
	/** The greenhouse the person is tending. */
	private Farming greenhouse;
	/** The building where the greenhouse the person is tending. */
	private Building farmBuilding;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public TendGreenhouse(Person person) {
		// Use Task constructor
		super(NAME, person, false, false, STRESS_MODIFIER, true, 20D);

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		farmBuilding = getAvailableGreenhouse(person);
		if (farmBuilding != null) {
			greenhouse = farmBuilding.getFarming();

			// Walk to greenhouse.
			walkToActivitySpotInBuilding(farmBuilding, false);
			
			if (person != null) {

				int rand = RandomUtil.getRandomInt(20);

				if (rand == 0) {
					
					addPhase(INSPECTING);
					setPhase(INSPECTING);
				} 
				
				else if (rand == 1) {
					
					addPhase(CLEANING);
					setPhase(CLEANING);
				}
				
				else if (rand == 2) {
					
					addPhase(SAMPLING);
					setPhase(SAMPLING);
				}

				else if (rand == 3) {
					
					addPhase(GROWING_TISSUE);
					setPhase(GROWING_TISSUE);
				}
				
				else if (rand == 4) {
					
					if (greenhouse.getNumCrops2Plant() > 0) {
					
						addPhase(TRANSFERRING_SEEDLING);
						setPhase(TRANSFERRING_SEEDLING);
					}
					
					else
						addPhase(TENDING);
						setPhase(TENDING);
				}
					
				else {
					addPhase(TENDING);
					setPhase(TENDING);
				}
			}
		} 
		
		else {
			endTask();
			return;
		}
	}

	/**
	 * Constructor 2.
	 * 
	 * @param robot the robot performing the task.
	 */
	public TendGreenhouse(Robot robot) {
		// Use Task constructor
		super(NAME, robot, false, false, 0, true, 50D);

		// Initialize data members
		if (robot.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		farmBuilding = getAvailableGreenhouse(robot);
		if (farmBuilding != null) {
			greenhouse = farmBuilding.getFarming();

			// Walk to greenhouse.
			walkToActivitySpotInBuilding(farmBuilding, false);
			
			// Initialize phase
			addPhase(TENDING);
			setPhase(TENDING);
			
		} else {
			endTask();
			return;
		}
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.FARMING;
	}

	public FunctionType getRoboticFunction() {
		return FunctionType.FARMING;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		} else if (INSPECTING.equals(getPhase())) {
			return inspectingPhase(time);
		} else if (CLEANING.equals(getPhase())) {
			return cleaningPhase(time);
		} else if (SAMPLING.equals(getPhase())) {
			return samplingPhase(time);
		} else if (TRANSFERRING_SEEDLING.equals(getPhase())) {
			return transferringSeedling(time);
		} else if (GROWING_TISSUE.equals(getPhase())) {
			return growingTissue(time);	
		} else {
			return time;
		}
	}

	/**
	 * Performs the tending phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double tendingPhase(double time) {

		double workTime = time;

		if (isDone()) {
			return time;
		}

		// Check if greenhouse has malfunction.
		if (greenhouse.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double mod = 0;

		if (person != null) {
			mod = 6D;
		}

		else {//if (robot != null) {
			mod = 4D;
		}

		// Determine amount of effective work time based on "Botany" skill
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill == 0) {
			mod += RandomUtil.getRandomDouble(.25);
		} else {
			mod += RandomUtil.getRandomDouble(.25) + 1.25 * greenhouseSkill;
		}

		workTime *= mod;

		// Divided by mod to get back any leftover real time
		workTime = greenhouse.addWork(workTime, this, robot)/mod;
		
		// Add experience
		addExperience(time);

		// Check for accident in greenhouse.
		checkForAccident(time);

		double remainingTime = time - workTime;
		
		if (remainingTime > 0)
			return remainingTime;
		
		return 0;
	}

	public void setCropDescription(Crop needyCrop) {
		setDescription(Msg.getString("Task.description.tendGreenhouse.tend",
				Conversion.capitalize(needyCrop.getCropName())));

	}

	
	private double transferringSeedling(double time) {
		setDescription(Msg.getString("Task.description.tendGreenhouse.transfer"));
		greenhouse.transferSeedling(time, person);
		
		return 0;		
	}

	private double growingTissue(double time) {

		if (person != null) {
				
			// Obtain the crop with the highest VP to work on in the lab
			CropType type = greenhouse.selectVPCrop();
				
			if (greenhouse.checkBotanyLab(type.getID(), person))  {
				
				LogConsolidated.log(Level.INFO, 30_000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was growing " + type.getName() + " tissue culture in the botany lab in " 
						+ farmBuilding.getNickName()
						+ ".");
				return 0;
			}
		}
	
		return time;
	}
	
	/**
	 * Performs the inspecting phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double inspectingPhase(double time) {

		List<String> uninspected = greenhouse.getUninspected();
		int size = uninspected.size();

		if (size > 0) {
			int rand = RandomUtil.getRandomInt(size - 1);

			String goal = uninspected.get(rand);

			greenhouse.markInspected(goal);

			setDescription(Msg.getString("Task.description.tendGreenhouse.inspect", goal));
		}

		return 0;
	}

	/**
	 * Performs the cleaning phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double cleaningPhase(double time) {

		List<String> uncleaned = greenhouse.getUncleaned();
		int size = uncleaned.size();

		if (size > 0) {
			int rand = RandomUtil.getRandomInt(size - 1);

			String goal = uncleaned.get(rand);

			greenhouse.markCleaned(goal);

			setDescription(Msg.getString("Task.description.tendGreenhouse.clean", goal));
		}

		return 0;
	}

	/**
	 * Performs the sampling phase in the botany lab
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double samplingPhase(double time) {

		CropType type = null;
		
		int rand = RandomUtil.getRandomInt(5);

		if (rand == 0) {
			// Obtain a crop type randomly
			type = CropConfig.getRandomCropType();
		}
			
		else {
			// Obtain the crop type with the highest VP to work on in the lab
			type = greenhouse.selectVPCrop();		
		}

		if (type != null) {

			if (person != null) {
				boolean hasWork = greenhouse.checkBotanyLab(type.getID(), person);
	
				if (hasWork) {
					setDescription(Msg.getString("Task.description.tendGreenhouse.sample",
						Conversion.capitalize(type.getName()) + " Tissues Culture for Lab Work"));

					LogConsolidated.log(Level.INFO, 30_000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was growing and sampling " + type.getName() + " tissue culture in the botany lab in " 
						+ farmBuilding.getNickName()
						+ ".");
				}
	
			}
		}

		return 0;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Botany" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 100D;
		int experienceAptitude = 0;
		if (person != null)
			experienceAptitude = person.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		else if (robot != null)
			experienceAptitude = robot.getRoboticAttributeManager()
					.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);

		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		if (person != null)
			person.getSkillManager().addExperience(SkillType.BOTANY, newPoints, time);
		else if (robot != null)
			robot.getSkillManager().addExperience(SkillType.BOTANY, newPoints, time);

	}

	/**
	 * Check for accident in greenhouse.
	 * 
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .005D;

		// Greenhouse farming skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the wear condition.
		chance *= greenhouse.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
//	            logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " + person.getName() + " has an accident while tending greenhouse.");
				farmBuilding.getMalfunctionManager().createASeriesOfMalfunctions(person);
			} else if (robot != null) {
//				logger.info("[" + robot.getLocationTag().getShortLocationName() +  "] " + robot.getName() + " has an accident while tending greenhouse.");
				farmBuilding.getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}
		}
	}

	/**
	 * Gets the greenhouse the person is tending.
	 * 
	 * @return greenhouse
	 */
	public Farming getGreenhouse() {
		return greenhouse;
	}

	/**
	 * Gets an available greenhouse that the person can use. Returns null if no
	 * greenhouse is currently available.
	 * 
	 * @param person the person
	 * @return available greenhouse
	 */
	public static Building getAvailableGreenhouse(Unit unit) {
		Building result = null;
		Person person = null;
		Robot robot = null;
		BuildingManager buildingManager;

		if (unit instanceof Person) {
			person = (Person) unit;
			if (person.isInSettlement()) {
				buildingManager = person.getSettlement().getBuildingManager();
				// List<Building> farmBuildings =
				// buildingManager.getBuildings(BuildingFunction.FARMING);
				// farmBuildings = BuildingManager.getNonMalfunctioningBuildings(farmBuildings);
				// farmBuildings = BuildingManager.getFarmsNeedingWork(farmBuildings);
				// farmBuildings = BuildingManager.getLeastCrowdedBuildings(farmBuildings);
				List<Building> farmBuildings = buildingManager.getFarmsNeedingWork();

				if (farmBuildings != null) {
					if (!farmBuildings.isEmpty()) {
						if (farmBuildings.size() > 0) {
							Map<Building, Double> farmBuildingProbs = BuildingManager
									.getBestRelationshipBuildings(person, farmBuildings);
							result = RandomUtil.getWeightedRandomObject(farmBuildingProbs);
						}
					}
				}
			}
		}

		else if (unit instanceof Robot) {
			robot = (Robot) unit;
			if (robot.isInSettlement()) {
				buildingManager = robot.getSettlement().getBuildingManager();
				List<Building> farmBuildings = buildingManager.getFarmsNeedingWork();

				// Choose the building the robot is at.
				if (farmBuildings != null) {
					if (!farmBuildings.isEmpty()) {
						for (Building b : farmBuildings) {
							if (b == robot.getBuildingLocation())
								return b;
							// TODO: choose the farmBuilding closest to the robot
							// TODO: check if other robots are already in this farmBuilding, i.e. checking
							// for the crowdliness of this farmBuilding
						}

						if (farmBuildings.size() > 0) {
							result = farmBuildings.get(RandomUtil.getRandomInt(0, farmBuildings.size() - 1));
						}
					}
				}

				// TODO: add person's good/bad feeling toward robots
//                int size = farmBuildings.size();
//                //System.out.println("size is "+size);
//                int selected = 0;
//                if (size == 0)
//                	result = null;
//                if (size >= 1) {
//                	selected = RandomUtil.getRandomInt(size-1);
//                	result = farmBuildings.get(selected);
//                }
				// System.out.println("getAvailableGreenhouse() : selected is "+selected);
			}
		}
		return result;
	}

	@Override
	// TODO: get agility score of a person/robot
	public int getEffectiveSkillLevel() {	
		if (person != null)
			return person.getEffectiveSkillLevel(SkillType.BOTANY);
		else if (robot != null)
			return robot.getEffectiveSkillLevel(SkillType.BOTANY);
		return 0;
//		SkillManager skillManager = null;
//		if (person != null)
//			skillManager = person.getSkillManager();
//		else if (robot != null)
//			skillManager = robot.getSkillManager();
//
//		return skillManager.getEffectiveSkillLevel(SkillType.BOTANY);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.BOTANY);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();
		greenhouse.destroy();
		greenhouse = null;
		farmBuilding.destroy();
		farmBuilding = null;
	}
}