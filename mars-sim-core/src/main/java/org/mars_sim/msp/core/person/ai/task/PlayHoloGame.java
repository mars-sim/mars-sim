/*
 * Mars Simulation Project
 * PlayHoloGame.java
 * @date 2022-06-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
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
	private static final SimLogger logger = SimLogger.getLogger(PlayHoloGame.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.playHoloGame"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PLAYING_A_HOLO_GAME = new TaskPhase(Msg.getString("Task.phase.playHoloGame")); //$NON-NLS-1$

	private static final TaskPhase SETTING_UP_SCENES = new TaskPhase(Msg.getString("Task.phase.settingUpScenes")); //$NON-NLS-1$

	// Static members
    /** Computing Units requested. */		
	private double computingNeeded = RandomUtil.getRandomDouble(0.05, 1.0);
    
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.3D;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public PlayHoloGame(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, RandomUtil.getRandomInt(10, 20));

		// If during person's work shift, only relax for short period.
		int millisols = marsClock.getMillisolInt();
		boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
		if (isShiftHour) {
			setDuration(5D);
		}

		if (person.isInSettlement()) {
			// If person is in a settlement, try to find a place to relax.
			boolean walkSite = false;
			int rand = RandomUtil.getRandomInt(3);
			
			if (rand == 0) {
				// if rec building is not available, go to a gym
				Building gym = Workout.getAvailableGym(person);
				if (gym != null) {
					walkToActivitySpotInBuilding(gym, FunctionType.EXERCISE, true);
					walkSite = true;
				}
			}
			
			else if (rand == 1 || rand == 2) {
				Building rec = getAvailableRecreationBuilding(person);
				if (rec != null) {
					walkToActivitySpotInBuilding(rec, FunctionType.RECREATION, true);
					walkSite = true;
				}
			}
			
			// Still not got a destination
			if (!walkSite) {
				// Go back to his quarters
				Building quarters = person.getQuarters();
				if (quarters != null) {
					walkToBed(quarters, person, true);
					walkSite = true;
				}
				else 
					// Walk to random location.
					walkToRandomLocation(true);
			}
		}
		else {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
			}
			else {
				// Walk to random location.
				walkToRandomLocation(true);
			}
		}

		// Initialize phase
		addPhase(SETTING_UP_SCENES);
		addPhase(PLAYING_A_HOLO_GAME);

		setPhase(SETTING_UP_SCENES);

		logger.fine(person, "Setting up hologames to play.");
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
		
		int msol = marsClock.getMillisolInt();       
        boolean successful = false; 
        
        if (computingNeeded > 0) {
        	double randWork = 0; 
        	
        	if (computingNeeded <= .01) {
        		randWork = computingNeeded;
        	}
        	else {
        		randWork = RandomUtil.getRandomDouble(computingNeeded / 6.0, computingNeeded / 3.0);
        	}
        		
        	// Submit his request for computing resources
        	Computation center = person.getAssociatedSettlement().getBuildingManager().getMostFreeComputingNode(randWork/5.0, msol + 1, msol + 6);
        	if (center != null) {
        		if (computingNeeded <= .01)
        			successful = center.scheduleTask(randWork, msol + 1, msol + 2);
        		else
        			successful = center.scheduleTask(randWork/5.0, msol + 1, msol + 6);
        	}
	    	else
	    		logger.info(person, 10_000L, "No computing centers available for setting up the holo game.");
        	
        	if (successful) {
        		logger.info(person, 10_000L, "Utilized " 
        				+ Math.round(randWork * 100_000.0)/100_000.0 
        				+ " CUs for playing holo game.");
        		computingNeeded = computingNeeded - randWork;
        		 if (computingNeeded < 0) {
        			 computingNeeded = 0; 
        		 }
          	}
	    	else {
	    		logger.info(person, 10_000L, "No computing resources available for scheduling a holo game setup.");
	    	}
        }
        else if (computingNeeded <= 0) {
        	// this task has ended
        	endTask();
        }     
		
		// Either +ve or -ve
		double rand = RandomUtil.getRandomInt(1);
		if (rand == 0)
			rand = -1;

		 // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        
        if (hunger > 1000) {
        	endTask();
        }
        
		// Reduce stress but may increase or reduce a person's fatigue level
		double newFatigue = fatigue - (2D * time * rand);
		if (newFatigue < 0D)
			newFatigue = 0D;
		condition.setFatigue(newFatigue);
		
        // Reduce person's stress
        double stress = condition.getStress() - (2.5 * time);
        condition.setStress(stress);
        
		return 0D;
	}

	/**
	 * Performs the setting up phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double settingUpPhase(double time) {
		boolean successful = false; 
		int msol = marsClock.getMillisolInt();
		
		if (computingNeeded > 0) {
        	double randWork = 0; 
        	
        	if (computingNeeded <= .01) {
        		randWork = computingNeeded;
        	}
        	else {
        		randWork = RandomUtil.getRandomDouble(computingNeeded / 10.0, computingNeeded / 9.0);
        	}
			// Submit his request for computing resources
	    	Computation center = person.getAssociatedSettlement().getBuildingManager().getMostFreeComputingNode(randWork, msol + 1, msol + 2);
	    	if (center != null) {
	    		successful = center.scheduleTask(randWork, msol + 1, msol + 2);
	    	}
	    	if (successful) {
	    		logger.info(person, 10_000L, "Utilized " 
	    				+ Math.round(randWork * 100_000.0)/100_000.0 
	    				+ " CUs for setting up a holo game.");
	    		computingNeeded = computingNeeded - randWork;
	    	}
	    	else {
	    		logger.info(person, 10_000L, "No computing resources for setting up the holo game.");
	    	}
		}
		
		if (getTimeCompleted() * 20.0 > getDuration()) {
			// If it has spent 1/20 of its time
			setPhase(PLAYING_A_HOLO_GAME);
		}
		
		return time * .95;
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
}
