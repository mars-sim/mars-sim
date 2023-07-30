/*
 * Mars Simulation Project
 * PlayHoloGame.java
 * @date 2022-07-18
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This task lowers the stress and may increase or decrease fatigue. The
 * duration of the task is by default chosen randomly, up to 100 millisols.
 */
public class PlayHoloGame extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PlayHoloGame.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.playHoloGame"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PLAYING_A_HOLO_GAME = new TaskPhase(Msg.getString("Task.phase.playHoloGame")); //$NON-NLS-1$

	private static final TaskPhase SETTING_UP_SCENES = new TaskPhase(Msg.getString("Task.phase.settingUpScenes")); //$NON-NLS-1$
    		
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.3D;
	
	// Data members
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed = RandomUtil.getRandomDouble(.005, 0.05);
	
	private final double TOTAL_COMPUTING_NEEDED;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public PlayHoloGame(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, RandomUtil.getRandomInt(10, 20));
		
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		// If during person's work shift, only relax for short period.
		boolean isShiftHour = person.isOnDuty();
		if (isShiftHour) {
			setDuration(5D);
		}

		if (person.isInSettlement()) {
			// If person is in a settlement, try to find a place to relax.
			boolean walkSite = false;
			int rand = RandomUtil.getRandomInt(3);
			
			if (rand == 0) {
				// if rec building is not available, go to a gym
				Building gym = BuildingManager.getAvailableGymBuilding(person);
				if (gym != null) {
					walkToActivitySpotInBuilding(gym, FunctionType.EXERCISE, true);
					walkSite = true;
				}
			}
			
			else if (rand == 1 || rand == 2) {
				Building rec = BuildingManager.getAvailableRecBuilding(person);
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
	 * Performs the setting up phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double settingUpPhase(double time) {

		setPhase(PLAYING_A_HOLO_GAME);

		return 0;
	}
	
	/**
	 * Performs the playing phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double playingPhase(double time) {
		double remainingTime = 0;
		
		if (!person.getPhysicalCondition().isNominallyFit()) {
			endTask();
		}
		
		if (isDone() || getTimeCompleted() + time > getDuration() || computingNeeded <= 0) {
        	// this task has ended
	  		logger.info(person, 30_000L, NAME + " - " 
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		int msol = getMarsTime().getMillisolInt(); 
              
        computingNeeded = person.getAssociatedSettlement().getBuildingManager().
            	accessNode(person, computingNeeded, time, seed, 
            			msol, getDuration(), NAME);

        // Add experience
        addExperience(time);
        
		// Either +ve or -ve
		double rand = RandomUtil.getRandomInt(1);
		if (rand == 0)
			rand = -1;

		 // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
		// May either increase or reduce a person's fatigue level
		condition.increaseFatigue(2 * time * rand);
        // Reduce person's stress
        condition.reduceStress(2 * time);
        
        if (condition.getHunger() > 666 || condition.getFatigue() > 666) {
	        endTask();
		}
        
		return remainingTime;
	}

}
