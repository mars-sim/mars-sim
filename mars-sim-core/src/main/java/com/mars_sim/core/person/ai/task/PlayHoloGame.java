/*
 * Mars Simulation Project
 * PlayHoloGame.java
 * @date 2022-07-18
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.Collections;

import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

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
	private static final ExperienceImpact IMPACT = new ExperienceImpact(1D, null, false, -0.3, Collections.emptySet());
	
	// Data members
    /** Computing Units needed per millisol. */		
	private ComputingJob compute;
    
	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public PlayHoloGame(Person person) {
		super(NAME, person, false, IMPACT, RandomUtil.getRandomInt(10, 20));
		
		// If during person's work shift, only relax for short period.
		boolean isShiftHour = person.isOnDuty();
		if (isShiftHour) {
			setDuration(5D);
		}

		if (person.isInSettlement()) {
			// If person is in a settlement, try to find a place to relax.
			FunctionType target = (RandomUtil.getRandomInt(3) == 0 ? FunctionType.EXERCISE
												: FunctionType.RECREATION);
			Building rec = BuildingManager.getAvailableFunctionTypeBuilding(person, target);
			if (rec != null) {
				walkToActivitySpotInBuilding(rec, target, true);
			}
			
			// Still not got a destination, go back to bed
			else if (person.hasBed()) {
				// Walk to the bed
				walkToBed(person, true);
			}
			else 
				// Walk to random location.
				walkToRandomLocation(true);
		}
		else {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover r) {
				walkToPassengerActivitySpotInRover(r, true);
			}
			else {
				// Walk to random location.
				walkToRandomLocation(true);
			}
		}
		
        int now = getMarsTime().getMillisolInt();
        
        this.compute = new ComputingJob(person.getAssociatedSettlement(), ComputingLoadType.MID, now, getDuration(), NAME);

        compute.pickMultipleNodes(0, now);
				
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
		
		if (person.getPhysicalCondition().isNominallyUnfit()) {
			endTask();
			return 0;
		}
		
		compute.process(getTimeCompleted(), getMarsTime().getMillisolInt());

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
        condition.reduceStress(time/2);
        
        if (condition.getHunger() > 666 || condition.getFatigue() > 666) {
	        endTask();
		}
        
		return remainingTime;
	}

}
