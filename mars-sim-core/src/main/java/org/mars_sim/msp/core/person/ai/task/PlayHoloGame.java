/**
 * Mars Simulation Project
 * PlayHoloGame.java
 * @version 3.1.0 2017-09-13
 * @author Manny Kung
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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This task lowers the stress and may increase or decrease fatigue. The
 * duration of the task is by default chosen randomly, up to 100 millisols.
 */
public class PlayHoloGame extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PlayHoloGame.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.playHoloGame"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PLAYING_A_HOLO_GAME = new TaskPhase(Msg.getString("Task.phase.playHoloGame")); //$NON-NLS-1$

	private static final TaskPhase SETTING_UP_SCENES = new TaskPhase(Msg.getString("Task.phase.settingUpScenes")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.3D;

//	private static Simulation sim = Simulation.instance();
//	private static MasterClock masterClock = sim.getMasterClock();
//	private static MarsClock marsClock = masterClock.getMarsClock();

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public PlayHoloGame(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(40D));

//        if (masterClock == null)
//        	masterClock = sim.getMasterClock();
//        
//		if (marsClock == null)
//			marsClock = masterClock.getMarsClock();// needed for loading a saved sim 

		// If during person's work shift, only relax for short period.
		int millisols = marsClock.getMillisolInt();
		boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
		if (isShiftHour) {
			setDuration(5D);
		}

		// If person is in a settlement, try to find a place to relax.
		boolean walkSite = false;
		
		if (person.isInSettlement()) {
			try {
				Building recBuilding = getAvailableRecreationBuilding(person);
				if (recBuilding != null) {
					// Walk to recreation building.
					// Add BuildingFunction.RECREATION
					walkToActivitySpotInBuilding(recBuilding, FunctionType.RECREATION, true);
					walkSite = true;
				} else {
					// if rec building is not available, go to a gym
					Building gym = Workout.getAvailableGym(person);
					if (gym != null) {
						walkToActivitySpotInBuilding(gym, FunctionType.EXERCISE, true);
						walkSite = true;
					} else {
						// if gym is not available, go back to his quarters
						Building quarters = person.getQuarters();
						if (quarters != null) {
							walkToActivitySpotInBuilding(quarters, FunctionType.LIVING_ACCOMODATIONS, true);
							walkSite = true;
						}
						else
							endTask();
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "ReadingABook's constructor(): " + e.getMessage());
				endTask();
			}
		}

		if (!walkSite) {
			if (person.isInVehicle()) {
				// If person is in rover, walk to passenger activity spot.
				if (person.getVehicle() instanceof Rover) {
					walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
				}
			} else {
				// Walk to random location.
				walkToRandomLocation(true);
			}
		}

		// Initialize phase
		addPhase(SETTING_UP_SCENES);
		addPhase(PLAYING_A_HOLO_GAME);

		setPhase(SETTING_UP_SCENES);

		LogConsolidated.log(Level.FINE, 3_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
				+ person + " was setting up hologames to play in " + person.getLocationTag().getImmediateLocation());
		
	}

//	public PlayHoloGame(Robot robot) {
//		super(NAME, robot, false, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(20D));
//	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.LIVING_ACCOMODATIONS;// RECREATION;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("PlayHoloGame. Task phase is null");
		} else if (SETTING_UP_SCENES.equals(getPhase())) {
			return settingUpPhase(time);
		} else if (PLAYING_A_HOLO_GAME.equals(getPhase())) {
			return playingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the playing phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double playingPhase(double time) {

//		if (isDone()) {
//			LogConsolidated.log(Level.INFO, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
//					+ person + " was done playing hologames in " + person.getLocationTag().getImmediateLocation());
//		}
		
		// Either +ve or -ve
		double rand = RandomUtil.getRandomInt(1);
		if (rand == 0)
			rand = -1;

		// Reduce stress but may increase or reduce a person's fatigue level
		double newFatigue = person.getPhysicalCondition().getFatigue() - (2D * time * rand);
		if (newFatigue < 0D) {
			newFatigue = 0D;
		}
		
		person.getPhysicalCondition().setFatigue(newFatigue);

		return 0D;
	}

	/**
	 * Performs the setting up phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double settingUpPhase(double time) {
		// TODO: add codes for selecting a particular type of game
		
//		LogConsolidated.log(Level.INFO, 3_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
//				+ person + " was setting up hologames to play in " + person.getLocationTag().getImmediateLocation());
//		
		setPhase(PLAYING_A_HOLO_GAME);
		return time * .8D;
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	/**
	 * Gets an available recreation building that the person can use. Returns null
	 * if no recreation building is currently available.
	 * 
	 * @param person the person
	 * @return available recreation building
	 */
	public static Building getAvailableRecreationBuilding(Person person) {

		Building result = null;

		if (person.isInSettlement()) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> recreationBuildings = manager.getBuildings(FunctionType.RECREATION);
			recreationBuildings = BuildingManager.getNonMalfunctioningBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getLeastCrowdedBuildings(recreationBuildings);

			if (recreationBuildings.size() > 0) {
				Map<Building, Double> recreationBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						recreationBuildings);
				result = RandomUtil.getWeightedRandomObject(recreationBuildingProbs);
			}
		}

		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}

//	/**
//	 * Reloads instances after loading from a saved sim
//	 * 
//	 * @param {@link MasterClock}
//	 * @param {{@link MarsClock}
//	 */
//	public static void initializeInstances(MasterClock c0, MarsClock c1) {
//		masterClock = c0;
//		marsClock = c1;
//	}

	@Override
	public void destroy() {
		super.destroy();
//		sim = null;
//		marsClock = null;
//		masterClock = null;
	}

}